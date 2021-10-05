package tr.com.cicerali.appiumhub;

public interface TestSessionInterceptor {
    default void afterTestSessionTerminate(TestSession testSession, SessionTerminationReason reason){}
    default void beforeTestSessionStart(TestSession testSession){}
}
