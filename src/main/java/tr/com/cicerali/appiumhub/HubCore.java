package tr.com.cicerali.appiumhub;

import com.google.common.math.DoubleMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import tr.com.cicerali.appiumhub.exception.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class HubCore {

    private static final Logger logger = LoggerFactory.getLogger(HubCore.class);

    private final Map<String, RemoteNode> remoteNodes = new ConcurrentHashMap<>();
    private final SessionManager sessionManager = new SessionManager();
    private final HubConfig hubConfig;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition nodeAvailable = lock.newCondition();

    public HubCore(HubConfig hubConfig) {
        this.hubConfig = hubConfig;
    }

    public RemoteNode getNodeById(String id) {
        return remoteNodes.get(id);
    }

    public Map<String, Object> getNodeStatus(String id) {
        RemoteNode remoteNode = getNodeById(id);
        if (remoteNode == null) {
            throw new NodeNotFoundException("Cannot find node with id: " + id);
        }
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("id", id);
        res.put("busy", remoteNode.isBusy());
        res.put("success", true);
        return res;
    }

    public void register(RegistrationRequest request) throws HubRegisterException {
        try {
            RemoteNode remoteNode = remoteNodes.get(request.getConfiguration().getId());
            if (remoteNode != null) {
                remoteNode.destroy(SessionTerminationReason.NODE_REREGISTRATION);
            }

            remoteNode = new RemoteNode(request, hubConfig.capabilityMatcher, this);
            remoteNodes.put(remoteNode.getId(), remoteNode);
            logger.info("Node successfully registered: {}\nconfiguration: {}\ncapabilities: {}", remoteNode.getId(), remoteNode.getConfiguration(), remoteNode.getCapabilities());
            wakeUpWaiters();
        } catch (Exception e) {
            throw new HubRegisterException(e);
        }
    }

    public void remove(RemoteNode remoteNode) {
        remoteNodes.remove(remoteNode.getId());
    }

    public SessionData processGetSessions() {
        return sessionManager.getAllSessions();
    }

    public ResponseEntity<byte[]> processRegularSession(HttpServletRequest request, String sessionKey) throws RequestParseException, SessionClosedException, SessionNotFoundException, ProxyForwardException, ProxyTimeoutException {

        RegularSessionRequest sessionRequest = new RegularSessionRequest(request, sessionKey);
        TestSession testSession = getSession(sessionRequest.getSessionKey());
        try {
            return forwardRequest(testSession, sessionRequest);
        } catch (ProxyForwardException e) {
            if (hubConfig.stopOnProxyError) {
                cleanSession(testSession, SessionTerminationReason.FORWARDING_TO_NODE_FAILED);
                wakeUpWaiters();
            }
            throw e;
        } catch (ProxyTimeoutException e) {
            if (hubConfig.stopOnProxyError) {
                cleanSession(testSession, SessionTerminationReason.SO_TIMEOUT);
                wakeUpWaiters();
            }
            throw e;
        }
    }

    public ResponseEntity<byte[]> processDeleteSession(HttpServletRequest request, String sessionKey) throws RequestParseException, SessionClosedException, SessionNotFoundException {

        RegularSessionRequest sessionRequest = new RegularSessionRequest(request, sessionKey, RequestType.DELETE_SESSION);
        TestSession testSession = getSession(sessionKey);

        ResponseEntity<byte[]> res = null;
        try {
            res = forwardRequest(testSession, sessionRequest);
        } catch (ProxyForwardException | ProxyTimeoutException e) {
            logger.error("Proxying the request(method: {}, uri: {}) failed, reason: {}", sessionRequest.getMethod(), sessionRequest.getRequestURI(), e.getMessage());
        }

        testSession.setStopped(true);
        cleanSession(testSession, SessionTerminationReason.CLIENT_STOPPED_SESSION);
        logger.info("Session({}) successfully deleted on node: {}", sessionKey, testSession.getRemoteNode().getId());
        wakeUpWaiters();
        return res;
    }

    public ResponseEntity<byte[]> processStartSession(HttpServletRequest request) throws HubSessionException, InterruptedException {

        StartSessionRequest sessionRequest = new StartSessionRequest(request);
        return processNewSessionRequest(sessionRequest);
    }

    private ResponseEntity<byte[]> forwardRequest(TestSession testSession, RegularSessionRequest sessionRequest) throws ProxyForwardException, ProxyTimeoutException {

        try {
            return testSession.forwardRegularRequest(sessionRequest);
        } catch (Exception e) {
            if (e.getCause() instanceof ResourceAccessException) {
                throw new ProxyTimeoutException(e);
            } else {
                throw new ProxyForwardException(e);
            }
        }
    }

    public void cleanSession(TestSession testSession, SessionTerminationReason reason) {
        sessionManager.removeSession(testSession, reason);
        testSession.getRemoteNode().clean();
    }

    public void wakeUpWaiters() {
        lock.lock();
        nodeAvailable.signalAll();
        lock.unlock();
    }

    private SessionTerminationReason getTerminationReason(String sessionKey) {
        return sessionManager.getTerminationReason(sessionKey);
    }

    private TestSession getSession(String sessionKey) throws SessionNotFoundException, SessionClosedException {
        TestSession testSession = sessionManager.getActiveSession(sessionKey);
        if (testSession == null) {
            SessionTerminationReason reason = getTerminationReason(sessionKey);
            if (reason == null) {
                throw new SessionNotFoundException("Session not found");
            } else {
                throw new SessionClosedException("Session " + sessionKey + " already closed, reason: " + reason);
            }
        }
        return testSession;
    }

    private ResponseEntity<byte[]> processNewSessionRequest(StartSessionRequest sessionRequest) throws HubSessionException, InterruptedException {

        logger.info("Trying to create new session with desired capabilities: {}", sessionRequest.getDesiredCapabilities());

        /* checking if any node has capability to handle this request
         * it is an initial check, if no matching it can throw an exception depending on configuration */
        verifyDesiredCapabilities(sessionRequest.getDesiredCapabilities());

        /* will try to create session on a node or throw exception because of timeout */
        TestSession testSession = getNewSession(sessionRequest);
        try {
            ResponseEntity<byte[]> response = testSession.forwardNewSessionRequest();
            sessionManager.addSession(testSession);
            logger.info("Session started on node: {}, session: {}", testSession.getRemoteNode().getId(), logger.isDebugEnabled() ? "\n" + testSession.getSessionData() : testSession.getSessionKey());
            return response;
        } catch (Exception e) {
            cleanSession(testSession, SessionTerminationReason.CREATION_FAILED);
            wakeUpWaiters();
            throw new SessionCreateException(e);
        }
    }

    @SuppressWarnings("java:S899")
    public TestSession getNewSession(StartSessionRequest sessionRequest) throws HubSessionException, InterruptedException {
        List<RemoteNode> sorted = getSorted();
        long begin = System.currentTimeMillis();
        long diff;
        RemoteNode remoteNode;

        do {
            remoteNode = sorted.stream().filter(n -> !n.isBusy() && !n.isDown()).filter(n -> n.lock.tryLock()).filter(n -> {
                boolean ret = n.hasCapability(sessionRequest.getDesiredCapabilities());
                if (!ret) {
                    n.lock.unlock();
                }
                return ret;
            }).findFirst().orElse(null);

            diff = System.currentTimeMillis() - begin;
            if (remoteNode != null) {
                break;
            }

            lock.lock();
            nodeAvailable.await(5, TimeUnit.SECONDS);
            lock.unlock();

        } while (diff <= hubConfig.newSessionWaitTimeout);

        if (remoteNode == null) {
            throw new SessionCreateException("Create session timeout");
        }
        TestSession testSession = new TestSession(sessionRequest, remoteNode, hubConfig.keepAuthorizationHeaders);
        remoteNode.setBusy(true);
        remoteNode.setTestSession(testSession);
        remoteNode.lock.unlock();
        logger.debug("Found a node for new session: {}", remoteNode.getId());
        return testSession;
    }

    public List<RemoteNode> getSorted() {
        List<RemoteNode> sorted = new ArrayList<>(remoteNodes.values());
        sorted.sort(proxyComparator);
        return sorted;
    }

    private final Comparator<RemoteNode> proxyComparator = (o1, o2) -> {
        double p1used = o1.getResourceUsageInPercent();
        double p2used = o2.getResourceUsageInPercent();

        double epsilon = 0.01;
        if (DoubleMath.fuzzyEquals(p1used, p2used, epsilon)) {
            long time1lastUsed = o1.getLastSessionStart();
            long time2lastUsed = o2.getLastSessionStart();
            if (time1lastUsed == time2lastUsed) return 0;
            return time1lastUsed < time2lastUsed ? -1 : 1;
        }
        return p1used < p2used ? -1 : 1;
    };

    public void verifyDesiredCapabilities(Map<String, Object> desiredCapabilities) throws HubSessionException {

        if (!hubConfig.throwOnCapabilityNotPresent) {
            return;
        }
        if (remoteNodes.isEmpty()) {
            throw new CapabilityNotFoundException("Empty hub, not possible to continue");
        }

        if (!hasCapability(desiredCapabilities)) {
            throw new CapabilityNotFoundException("Hub can not find capability: " + desiredCapabilities);
        }
    }

    public boolean hasCapability(Map<String, Object> requestedCapability) {
        return remoteNodes.values().stream().anyMatch(remoteNode -> remoteNode.hasCapability(requestedCapability));
    }

    public HubConfig getHubConfig() {
        return hubConfig;
    }
}
