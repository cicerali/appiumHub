package tr.com.cicerali.appiumhub;

import java.util.Optional;

public class HubConfig {

    public final int browserTimeout;
    public final int newSessionWaitTimeout;
    public final int cleanUpCycle;
    public final int timeout;
    public final int nodePolling;
    public final int downPollingLimit;
    public final int unregisterIfStillDownAfter;
    public final int nodeStatusCheckTimeout;
    public final boolean throwOnCapabilityNotPresent;

    public final CapabilityMatcher capabilityMatcher = (currentCapability, requestedCapability) -> false;

    public HubConfig(HubProperties hubProperties) {
        this.browserTimeout = Optional.ofNullable(hubProperties.getBrowserTimeout()).orElse(0);
        this.newSessionWaitTimeout = Optional.ofNullable(hubProperties.getNewSessionWaitTimeout()).orElse(-1);
        this.cleanUpCycle = Optional.ofNullable(hubProperties.getCleanUpCycle()).orElse(5000);
        this.timeout = Optional.ofNullable(hubProperties.getTimeout()).orElse(1800);
        this.nodePolling = Optional.ofNullable(hubProperties.getNodePolling()).orElse(5000);
        this.downPollingLimit = Optional.ofNullable(hubProperties.getDownPollingLimit()).orElse(2);
        this.unregisterIfStillDownAfter = Optional.ofNullable(hubProperties.getUnregisterIfStillDownAfter()).orElse(60000);
        this.nodeStatusCheckTimeout = Optional.ofNullable(hubProperties.getNodeStatusCheckTimeout()).orElse(5000);
        this.throwOnCapabilityNotPresent = Optional.ofNullable(hubProperties.getThrowOnCapabilityNotPresent()).orElse(true);
    }
}
