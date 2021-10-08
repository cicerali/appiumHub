package tr.com.cicerali.appiumhub;

import com.google.common.collect.ImmutableSet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SessionManager {

    /**
     * Maximum number of session termination history
     */
    private static final int MAX_REASON_MAP_SIZE = 1000;
    private final Map<String, TestSession> activeTestSessions = new ConcurrentHashMap<>();
    private final Queue<String> terminatedSessions = new ConcurrentLinkedQueue<>();
    private final Map<String, SessionTerminationReason> reasons = new ConcurrentHashMap<>();

    /**
     * It will return session data if still active
     *
     * @param sessionKey session id
     * @return test session
     */
    public TestSession getActiveSession(String sessionKey) {
        return activeTestSessions.get(sessionKey);
    }

    /**
     * It will return the session termination reason if it can find it in the history
     *
     * @param sessionKey session id
     * @return termination reason
     */
    public SessionTerminationReason getTerminationReason(String sessionKey) {
        return reasons.get(sessionKey);
    }

    /**
     * It will remove session from active session table,
     * and also will save its termination reason
     *
     * @param testSession test session
     * @param reason      session termination reason
     */
    public void removeSession(TestSession testSession, SessionTerminationReason reason) {
        String key = testSession.getSessionKey();
        if (key != null) {
            if (terminatedSessions.size() >= MAX_REASON_MAP_SIZE) {
                reasons.remove(terminatedSessions.poll());
            }
            terminatedSessions.offer(key);
            reasons.put(key, reason);
            testSession.setStopped(true);
            activeTestSessions.remove(key);
        }
    }

    /**
     * It will save session to active session table
     *
     * @param testSession test session
     */
    public void addSession(TestSession testSession) {
        activeTestSessions.put(testSession.getSessionKey(), testSession);
    }

    /**
     * It will return all active sessions
     *
     * @return set of active sessions
     */
    public Set<TestSession> getActiveTestSessions() {
        return ImmutableSet.copyOf(activeTestSessions.values());
    }

    /**
     * It will return all session metadata which collected from remote nodes
     *
     * @return session data
     */
    public SessionData getAllSessionData() {

        List<Map<String, Object>> ref = new ArrayList<>();
        activeTestSessions.forEach((k, v) -> ref.add(v.getSessionData()));
        return new SessionData(ref, 0);
    }
}
