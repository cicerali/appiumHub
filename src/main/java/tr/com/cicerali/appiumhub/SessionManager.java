package tr.com.cicerali.appiumhub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SessionManager {

    private static final int MAX_REASON_MAP_SIZE = 1000;
    private final Map<String, TestSession> activeTestSessions = new ConcurrentHashMap<>();
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
            activeTestSessions.remove(key);
        }
    }

    public void addSession(TestSession testSession) {
        activeTestSessions.put(testSession.getSessionKey(), testSession);
    }

    public SessionData getAllSessions() {

        List<Map<String, Object>> ref = new ArrayList<>();
        activeTestSessions.forEach((k, v) -> ref.add(v.getSessionData()));
        return new SessionData(ref, 0);
    }

}
