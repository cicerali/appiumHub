package tr.com.cicerali.appiumhub;

import com.google.common.math.DoubleMath;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class HubCore {

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
            throw new NodeNotFoundException("Cannot find proxy with ID =" + id + " in the registry.");
        }
        Map<String, Object> res = new TreeMap<>();
        res.put("msg", "proxy found !");
        res.put("success", true);
        res.put("id", id);
        res.put("request", remoteNode.getOriginalRegistrationRequest());
        return res;
    }

    public void register(RegistrationRequest request) throws HubRegisterException {
        try {
            RemoteNode remoteNode = remoteNodes.remove(request.getConfiguration().getId());
            if (remoteNode != null) {
                TestSession testSession = remoteNode.getTestSession();
                if (testSession != null) {
                    cleanSession(testSession, SessionTerminationReason.NODE_REREGISTRATION);
                }
                remoteNode.destroy();
            }

            remoteNodes.put(request.getConfiguration().getId(), new RemoteNode(request, hubConfig.capabilityMatcher, this));

            wakeUpWaiters();
        } catch (Exception e) {
            throw new HubRegisterException(e);
        }
    }

    public SessionData processGetSessions() {
        return sessionManager.getAllSessions();
    }

    public ResponseEntity<byte[]> processRegularSession(HttpServletRequest request, String sessionKey) throws HubSessionException {
        RegularSessionRequest sessionRequest = new RegularSessionRequest(request, sessionKey);
        return forwardRequest(sessionRequest);
    }

    public ResponseEntity<byte[]> processDeleteSession(HttpServletRequest request, String sessionKey) throws HubSessionException {
        RegularSessionRequest sessionRequest = new RegularSessionRequest(request, sessionKey, RequestType.DELETE_SESSION);
        ResponseEntity<byte[]> res = forwardRequest(sessionRequest);
        TestSession testSession = getSession(sessionKey);
        cleanSession(testSession, SessionTerminationReason.CLIENT_STOPPED_SESSION);
        wakeUpWaiters();
        return res;
    }

    public ResponseEntity<byte[]> processStartSession(HttpServletRequest request) throws HubSessionException, InterruptedException {
        StartSessionRequest sessionRequest = new StartSessionRequest(request);
        return processNewSessionRequest(sessionRequest);
    }

    private ResponseEntity<byte[]> forwardRequest(RegularSessionRequest sessionRequest) throws HubSessionException {
        TestSession testSession = getSession(sessionRequest.getSessionKey());
        if (testSession == null) {
            SessionTerminationReason reason = getTerminationReason(sessionRequest.getSessionKey());
            if (reason == null) {
                throw new SessionNotFoundException("Session not found");
            } else {
                throw new SessionClosedException("Session " + sessionRequest.getSessionKey() + " already closed, reason: " + reason);
            }
        }

        try {
            return testSession.forwardRegularRequest(sessionRequest);
        } catch (Exception e) {

            if (e.getCause() instanceof ResourceAccessException) {
                cleanSession(testSession, SessionTerminationReason.SO_TIMEOUT);
            } else {
                cleanSession(testSession, SessionTerminationReason.FORWARDING_TO_NODE_FAILED);
            }
            wakeUpWaiters();
            throw new HubSessionException(e);
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

    private TestSession getSession(String sessionKey) {
        return sessionManager.getActiveSession(sessionKey);
    }

    private ResponseEntity<byte[]> processNewSessionRequest(StartSessionRequest sessionRequest) throws HubSessionException, InterruptedException {

        /* checking if any node has capability to handle this request
         * it is an initial check, if no matching it can throw an exception depending on configuration */
        verifyDesiredCapabilities(sessionRequest.getDesiredCapabilities());

        /* will try to create session on a node or throw exception because of timeout */
        TestSession testSession = getNewSession(sessionRequest);
        try {
            ResponseEntity<byte[]> response = testSession.forwardNewSessionRequest();
            sessionManager.addSession(testSession);
            return response;
        } catch (Exception e) {
            cleanSession(testSession, SessionTerminationReason.CREATION_FAILED);
            wakeUpWaiters();
            throw new HubSessionException(e);
        }
    }

    public TestSession getNewSession(StartSessionRequest sessionRequest) throws HubSessionException, InterruptedException {
        List<RemoteNode> sorted = getSorted();
        long begin = System.currentTimeMillis();
        long diff;
        RemoteNode remoteNode;

        do {
            remoteNode = sorted.stream().filter(n -> !n.isBusy()).filter(n -> n.lock.tryLock()).filter(n -> {
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
            throw new HubSessionException("Create session timeout");
        }
        TestSession testSession = new TestSession(sessionRequest, remoteNode);
        remoteNode.setBusy(true);
        remoteNode.setTestSession(testSession);
        remoteNode.lock.unlock();
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
            throw new HubSessionException("Empty grid " + desiredCapabilities);
        }

        if (!hasCapability(desiredCapabilities)) {
            throw new HubSessionException("Can not find capability: " + desiredCapabilities);
        }
    }

    public boolean hasCapability(Map<String, Object> requestedCapability) {
        return remoteNodes.values().stream().anyMatch(remoteNode -> remoteNode.hasCapability(requestedCapability));
    }

    public HubConfig getHubConfig() {
        return hubConfig;
    }

}
