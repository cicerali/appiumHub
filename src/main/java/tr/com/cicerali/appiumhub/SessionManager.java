package tr.com.cicerali.appiumhub;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SessionManager {

    private final int MAX_REASON_MAP_SIZE = 1000;
    private Map<String, TestSession> activeTestSessions = new ConcurrentHashMap<>();
    private final Queue<String> terminatedSessions = new ConcurrentLinkedQueue<>();
    private final Map<String, SessionTerminationReason> reasons = new ConcurrentHashMap<>();

    public TestSession getActiveSession(String sessionKey) {
        return activeTestSessions.get(sessionKey);
    }

    public SessionTerminationReason getTerminationReason(String sessionKey) {
        return reasons.get(sessionKey);
    }

    public void removeSession(TestSession testSession, SessionTerminationReason reason) {
        String key = testSession.getSessionKey();
        if (key != null) {
            if (terminatedSessions.size() >= MAX_REASON_MAP_SIZE) {
                reasons.remove(terminatedSessions.poll());
            }
            terminatedSessions.offer(key);
            reasons.put(key, reason);
            testSession.setStopped(true);
        }
    }
}
