package tr.com.cicerali.appiumhub;

import com.google.common.collect.ImmutableList;
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
import java.util.stream.Collectors;

public class HubCore {

    private static final Logger logger = LoggerFactory.getLogger(HubCore.class);

    private final Map<String, RemoteNode> remoteNodes = new ConcurrentHashMap<>();
    private final SessionManager sessionManager = new SessionManager();
    private final HubConfig hubConfig;

    private final ReentrantLock coreLock = new ReentrantLock();
    private final Condition nodeAvailable = coreLock.newCondition();

    public HubCore(HubConfig hubConfig) {
        this.hubConfig = hubConfig;
    }

    /**
     * @param id node id
     * @return node status as a map
     */
    public Map<String, Object> getNodeStatus(String id) {
        RemoteNode remoteNode = remoteNodes.get(id);
        if (remoteNode == null) {
            throw new NodeNotFoundException("Cannot find node with id: " + id);
        }
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("id", id);
        res.put("busy", remoteNode.isBusy());
        res.put("success", true);
        return res;
    }

    /**
     * It will try to register remote node
     *
     * @param request registration request
     * @throws HubRegisterException if registration fail
     */
    public void register(RegistrationRequest request) throws HubRegisterException {

        String id = request.getConfiguration().getId();
        RemoteNode remoteNode = remoteNodes.get(id);
        try {
            if (remoteNode != null) {
                remoteNode.tearDown(SessionTerminationReason.NODE_REREGISTRATION);
            }

            remoteNode = new RemoteNode(request, hubConfig.capabilityMatcher, this);
            remoteNodes.put(remoteNode.getId(), remoteNode);
            logger.info("Node successfully registered: {}\n-->capabilities: {}\n---->configuration: {}", remoteNode.getId(), remoteNode.getCapabilities(), remoteNode.getConfiguration());
            wakeUpWaiters();
        } catch (Exception e) {
            throw new HubRegisterException("Failed to register node: " + id, e.getCause());
        }
    }

    /**
     * It will process create new session request
     *
     * @param request session related http request
     * @return proxy response
     * @throws RequestParseException       if request parsing fail
     * @throws InterruptedException        if interrupted
     * @throws CapabilityNotFoundException if requested capabilities not found in pool
     * @throws SessionCreateException      if session create fail
     */
    public ResponseEntity<byte[]> processStartSession(HttpServletRequest request) throws RequestParseException, InterruptedException, CapabilityNotFoundException, SessionCreateException {

        CreateSessionRequest sessionRequest = new CreateSessionRequest(request);
        logger.info("Trying to create new session with desired capabilities: {}", sessionRequest.getDesiredCapabilities());

        /* checking if any node has capability to handle this request
         * it is an initial check, if no matching it can throw an exception depending on configuration */
        verifyDesiredCapabilities(sessionRequest.getDesiredCapabilities());

        /* will try to create session on a node or throw exception because of timeout */
        TestSession testSession = getNewSession(sessionRequest);
        try {
            hubConfig.testSessionInterceptors.forEach(i -> i.beforeTestSessionStart(testSession));
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

    /**
     * It will process all regular session requests which means session commands
     *
     * @param request    session related http request
     * @param sessionKey session id
     * @return proxy response
     * @throws RequestParseException    if request parsing fail
     * @throws SessionClosedException   if session already closed
     * @throws SessionNotFoundException if the session is inactive or not in the history table
     * @throws ProxyForwardException    if forwarding to remote node fail
     * @throws ProxyTimeoutException    if forwarding to remote node timeout
     */
    public ResponseEntity<byte[]> processRegularSession(HttpServletRequest request, String sessionKey) throws RequestParseException, SessionClosedException, SessionNotFoundException, ProxyForwardException, ProxyTimeoutException {

        RegularSessionRequest sessionRequest = new RegularSessionRequest(request, sessionKey);
        TestSession testSession = getSession(sessionRequest.getSessionKey());
        try {
            return forwardRequest(testSession, sessionRequest);
        } catch (ProxyForwardException | ProxyTimeoutException e) {
            if (hubConfig.stopOnProxyError) {
                cleanSession(testSession, (e instanceof ProxyForwardException) ? SessionTerminationReason.FORWARDING_TO_NODE_FAILED : SessionTerminationReason.SO_TIMEOUT);
                wakeUpWaiters();
            }
            throw e;
        }
    }

    /**
     * It will process delete session request
     *
     * @param request    session related http request
     * @param sessionKey session id
     * @return proxy response
     * @throws RequestParseException    if request parsing fail
     * @throws SessionClosedException   if session already closed
     * @throws SessionNotFoundException if the session is inactive or not in the history table
     * @throws ProxyTimeoutException    if forwarding to remote node fail
     * @throws ProxyForwardException    if forwarding to remote node timeout
     */
    public ResponseEntity<byte[]> processDeleteSession(HttpServletRequest request, String sessionKey) throws RequestParseException, SessionClosedException, SessionNotFoundException, ProxyTimeoutException, ProxyForwardException {

        RegularSessionRequest sessionRequest = new RegularSessionRequest(request, sessionKey, RequestType.DELETE_SESSION);
        TestSession testSession = getSession(sessionKey);

        ResponseEntity<byte[]> res;
        try {
            res = forwardRequest(testSession, sessionRequest);
        } finally {
            cleanSession(testSession, SessionTerminationReason.CLIENT_STOPPED_SESSION);
            wakeUpWaiters();
        }
        return res;
    }

    /**
     * @param id node id
     */
    public void remove(String id) {
        remoteNodes.remove(id);
    }

    /**
     * @return all session data
     */
    public SessionData processGetSessions() {
        return sessionManager.getAllSessionData();
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

    /**
     * It will clean related node and will remove session from active sessions
     *
     * @param testSession test session
     * @param reason      termination reason
     */
    public void cleanSession(TestSession testSession, SessionTerminationReason reason) {
        sessionManager.removeSession(testSession, reason);
        testSession.getRemoteNode().clean();
        hubConfig.testSessionInterceptors.forEach(i -> i.afterTestSessionTerminate(testSession, reason));
        logger.info("Session({}) successfully deleted on node: {}, reason: {}", testSession.getSessionKey(), testSession.getRemoteNode().getId(), reason);
    }

    /**
     * Send signal to waiters which waiting free node
     */
    public void wakeUpWaiters() {
        coreLock.lock();
        nodeAvailable.signalAll();
        coreLock.unlock();
    }

    /**
     * @param sessionKey session id
     * @return termination reason
     */
    public SessionTerminationReason getTerminationReason(String sessionKey) {
        return sessionManager.getTerminationReason(sessionKey);
    }

    private TestSession getSession(String sessionKey) throws SessionNotFoundException, SessionClosedException {
        TestSession testSession = sessionManager.getActiveSession(sessionKey);
        if (testSession == null) {
            SessionTerminationReason reason = getTerminationReason(sessionKey);
            if (reason == null) {
                throw new SessionNotFoundException("Session not found: " + sessionKey);
            } else {
                throw new SessionClosedException("Session " + sessionKey + " already closed, reason: " + reason);
            }
        }
        return testSession;
    }

    private void verifyDesiredCapabilities(Map<String, Object> desiredCapabilities) throws CapabilityNotFoundException {

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

    @SuppressWarnings("java:S899")
    private TestSession getNewSession(CreateSessionRequest sessionRequest) throws SessionCreateException, InterruptedException {

        List<RemoteNode> sorted = getSorted();
        long begin = System.currentTimeMillis();
        long diff;
        Optional<RemoteNode> remoteNode;

        do {
            /* Sort nodes from less used to most
             * Try to lock remote node because:
             * only one request can access a node for new session create
             * check the locked node if it has capability
             */
            remoteNode = sorted.stream().filter(n -> !n.isBusy() && !n.isDown()).filter(n -> n.accessLock.tryLock()).filter(n -> {
                boolean matches = false;
                try {
                    matches = hubConfig.capabilityMatcher.matches(n.getCapabilities(), sessionRequest.getDesiredCapabilities());
                } finally {
                    if (!matches) {
                        n.accessLock.unlock();
                    }
                }
                return matches;
            }).findFirst();

            diff = System.currentTimeMillis() - begin;
            if (remoteNode.isPresent()) {
                break;
            }

            try {
                coreLock.lock();
                nodeAvailable.await(5, TimeUnit.SECONDS);
            } finally {
                coreLock.unlock();
            }

        } while (diff <= hubConfig.newSessionWaitTimeout);

        if (!remoteNode.isPresent()) {
            throw new SessionCreateException("Create session timeout");
        }
        RemoteNode node = remoteNode.get();
        try {
            TestSession testSession = new TestSession(sessionRequest, node, hubConfig.keepAuthorizationHeaders);
            node.setBusy(true);
            node.setTestSession(testSession);
            logger.debug("Found a node for new session: {}", node.getId());
            return testSession;
        } finally {
            node.accessLock.unlock();
        }
    }

    private List<RemoteNode> getSorted() {
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

    private boolean hasCapability(Map<String, Object> requestedCapability) {
        return remoteNodes.values()
                .stream()
                .anyMatch(remoteNode -> hubConfig.capabilityMatcher.matches(remoteNode.getCapabilities(), requestedCapability));
    }

    public HubConfig getHubConfig() {
        return hubConfig;
    }

    /**
     * @param sessionKey session id
     * @throws SessionClosedException   if session already closed
     * @throws SessionNotFoundException if the session is inactive or not in the history table
     */
    public void terminateTestSession(String sessionKey) throws SessionClosedException, SessionNotFoundException {
        cleanSession(getSession(sessionKey), SessionTerminationReason.ORPHAN);
    }

    /**
     * @return remote nodes as list
     */
    public List<RemoteNode> getAllNodes() {
        return ImmutableList.copyOf(remoteNodes.values());
    }

    /**
     * @return reachable remote nodes as list
     */
    public List<RemoteNode> getAllReachableNodes() {
        return remoteNodes.values().stream().filter(n -> !n.isDown()).collect(Collectors.toList());
    }

    /**
     * @return used remote nodes as list
     */
    public List<RemoteNode> getUsedNodes() {
        return remoteNodes.values().stream().filter(RemoteNode::isBusy).collect(Collectors.toList());
    }

    /**
     * @return active session count
     */
    public int getActiveSessionCount() {
        return sessionManager.getActiveTestSessions().size();
    }

    /**
     * @return active session set
     */
    public Set<TestSession> getActiveSessions() {
        return sessionManager.getActiveTestSessions();
    }
}
