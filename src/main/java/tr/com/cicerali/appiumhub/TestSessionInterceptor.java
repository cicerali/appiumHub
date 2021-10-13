package tr.com.cicerali.appiumhub;

/**
 * Interceptor for some test session requests.
 */
public interface TestSessionInterceptor {

    /**
     * Called just after test session termination
     *
     * @param testSession test session
     * @param reason      session termination reason
     */
    void afterTestSessionTerminate(TestSession testSession, SessionTerminationReason reason);

    /**
     * Called just before test session start
     *
     * @param testSession test session
     */
    void beforeTestSessionStart(TestSession testSession);
}
