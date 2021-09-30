package tr.com.cicerali.appiumhub;

import java.util.Optional;

import static tr.com.cicerali.appiumhub.Constants.*;

public class HubConfig {

    public final String pathPrefix;
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

    public static final CapabilityMatcher testMatcher = (currentCapability, requestedCapability) -> true;

    public HubConfig(HubProperties hubProperties) {

        this.pathPrefix = Optional.ofNullable(hubProperties.getPathPrefix()).orElse(DEFAULT_PATH_PREFIX);
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
        this.capabilityMatcher = new DefaultCapabilityMatcher(true);
    }
}
