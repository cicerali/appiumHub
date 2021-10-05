package tr.com.cicerali.appiumhub;

import org.springframework.http.client.ClientHttpRequestInterceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static tr.com.cicerali.appiumhub.Constants.*;

public class HubConfig {

    public final String pathPrefix;
    public final boolean keepAuthorizationHeaders;
    public final boolean stopOnProxyError;
    public final int browserTimeout;
    public final int newSessionWaitTimeout;
    public final int cleanUpCycle;
    public final int timeout;
    public final int nodePolling;
    public final int downPollingLimit;
    public final int unregisterIfStillDownAfter;
    public final int nodeStatusCheckTimeout;
    public final boolean throwOnCapabilityNotPresent;
    public final CapabilityMatcher capabilityMatcher;
    private final List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
    private TestSessionInterceptor testSessionInterceptor;

    public static final CapabilityMatcher testMatcher = (currentCapability, requestedCapability) -> true;

    public HubConfig(HubProperties hubProperties) {
        this(hubProperties, new DefaultCapabilityMatcher(true));
    }

    public HubConfig(HubProperties hubProperties, CapabilityMatcher capabilityMatcher) {

        this.pathPrefix = Optional.ofNullable(hubProperties.getPathPrefix()).orElse(DEFAULT_PATH_PREFIX);
        this.keepAuthorizationHeaders = Optional.ofNullable(hubProperties.getKeepAuthorizationHeaders()).orElse(DEFAULT_KEEP_AUTHORIZATION_HEADERS);
        this.stopOnProxyError = Optional.ofNullable(hubProperties.getStopOnProxyError()).orElse(DEFAULT_STOP_ON_PROXY_ERROR);
        this.browserTimeout = Optional.ofNullable(hubProperties.getBrowserTimeout()).orElse(DEFAULT_BROWSER_TIMEOUT);
        this.newSessionWaitTimeout = Optional.ofNullable(hubProperties.getNewSessionWaitTimeout()).orElse(DEFAULT_NEW_SESSION_WAIT_TIMEOUT);
        this.cleanUpCycle = Optional.ofNullable(hubProperties.getCleanUpCycle()).orElse(DEFAULT_CLEAN_UP_CYCLE_INTERVAL);
        this.timeout = Optional.ofNullable(hubProperties.getTimeout()).orElse(DEFAULT_SESSION_TIMEOUT);
        this.nodePolling = Optional.ofNullable(hubProperties.getNodePolling()).orElse(DEFAULT_POLLING_INTERVAL);
        this.downPollingLimit = Optional.ofNullable(hubProperties.getDownPollingLimit()).orElse(DEFAULT_DOWN_POLLING_LIMIT);
        this.unregisterIfStillDownAfter = Optional.ofNullable(hubProperties.getUnregisterIfStillDownAfter()).orElse(DEFAULT_UNREGISTER_DELAY);
        this.nodeStatusCheckTimeout = Optional.ofNullable(hubProperties.getNodeStatusCheckTimeout()).orElse(DEFAULT_NODE_STATUS_CHECK_TIMEOUT);
        this.throwOnCapabilityNotPresent = Optional.ofNullable(hubProperties.getThrowOnCapabilityNotPresent()).orElse(DEFAULT_THROW_ON_CAPABILITY_NOT_PRESENT);
        this.capabilityMatcher = capabilityMatcher;
    }

    public List<ClientHttpRequestInterceptor> getInterceptors() {
        return interceptors;
    }

    public void addInterceptor(ClientHttpRequestInterceptor interceptor) {
        interceptors.add(interceptor);
    }

    public void setTestSessionInterceptor(TestSessionInterceptor testSessionInterceptor) {
        this.testSessionInterceptor = testSessionInterceptor;
    }

    public TestSessionInterceptor getTestSessionInterceptor() {
        if (testSessionInterceptor == null) {
            return new TestSessionInterceptor() {
            };
        }
        return testSessionInterceptor;
    }
}
