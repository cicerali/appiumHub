package tr.com.cicerali.appiumhub;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("appium-hub")
public class HubProperties {

    /**
     * <Integer> in seconds : number of seconds a test session is allowed to
     *        hang while a WebDriver command is running (example: driver.get(url)). If the
     *        timeout is reached while a WebDriver command is still processing, the session
     *        will quit. Minimum value is 60. An unspecified, zero, or negative value means
     *        wait indefinitely.
     *        Default: 0
     */
    private Integer browserTimeout;

    /**
     * <Integer> in ms : The time after which a new test waiting for a node to
     *        become available will time out. When that happens, the test will throw an
     *        exception before attempting to start it. An unspecified, zero, or negative
     *        value means wait indefinitely.
     *        Default: -1
     */
    private Integer newSessionWaitTimeout;

    /**
     * <Integer> in ms : Specifies how often the hub will poll running proxies
     *        for timed-out (i.e. hung) threads. Must also specify "timeout" option.
     *        Default: 5000
     */
    private Integer cleanUpCycle;

    /**
     * <Integer> in seconds : Specifies the timeout before the server
     *        automatically kills a session that hasn't had any activity in the last X seconds.
     *        The test slot will then be released for another test to use. This is typically
     *        used to take care of client crashes. Must also specify "cleanUpCycle" option.
     *        Also known as "sessionTimeout"
     *        Default: 1800
     */
    private Integer timeout;

    /**
     * <Integer> in ms : specifies how often the hub will poll to see if the
     *        node is still responding.
     *        Default: 5000
     */
    private Integer nodePolling;

    /**
     * <Integer> : node is marked as "down" if the node hasn't responded after
     *        the number of checks specified in [downPollingLimit].
     *        Default: 2
     */
    private Integer downPollingLimit;

    /**
     * <Integer> in ms : if the node remains down for more than
     *        [unregisterIfStillDownAfter] ms, it will stop attempting to re-register from the hub.
     *        Default: 60000
     */
    private Integer unregisterIfStillDownAfter;

    /**
     * <Integer> in ms : connection/socket timeout, used for node "nodePolling"
     *        check.
     *        Default: 5000
     */
    private Integer nodeStatusCheckTimeout;

    /**
     * <Boolean> true or false : If true, the hub will reject all test requests
     *        if no compatible proxy is currently registered. If set to false, the request
     *        will queue until a node supporting the capability is registered with the grid.
     *        Default: true
     */
    private Boolean throwOnCapabilityNotPresent;

    public Integer getBrowserTimeout() {
        return browserTimeout;
    }

    public void setBrowserTimeout(Integer browserTimeout) {
        this.browserTimeout = browserTimeout;
    }

    public Integer getNewSessionWaitTimeout() {
        return newSessionWaitTimeout;
    }

    public void setNewSessionWaitTimeout(Integer newSessionWaitTimeout) {
        this.newSessionWaitTimeout = newSessionWaitTimeout;
    }

    public Integer getCleanUpCycle() {
        return cleanUpCycle;
    }

    public void setCleanUpCycle(Integer cleanUpCycle) {
        this.cleanUpCycle = cleanUpCycle;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getNodePolling() {
        return nodePolling;
    }

    public void setNodePolling(Integer nodePolling) {
        this.nodePolling = nodePolling;
    }

    public Integer getDownPollingLimit() {
        return downPollingLimit;
    }

    public void setDownPollingLimit(Integer downPollingLimit) {
        this.downPollingLimit = downPollingLimit;
    }

    public Integer getUnregisterIfStillDownAfter() {
        return unregisterIfStillDownAfter;
    }

    public void setUnregisterIfStillDownAfter(Integer unregisterIfStillDownAfter) {
        this.unregisterIfStillDownAfter = unregisterIfStillDownAfter;
    }

    public Integer getNodeStatusCheckTimeout() {
        return nodeStatusCheckTimeout;
    }

    public void setNodeStatusCheckTimeout(Integer nodeStatusCheckTimeout) {
        this.nodeStatusCheckTimeout = nodeStatusCheckTimeout;
    }

    public Boolean getThrowOnCapabilityNotPresent() {
        return throwOnCapabilityNotPresent;
    }

    public void setThrowOnCapabilityNotPresent(Boolean throwOnCapabilityNotPresent) {
        this.throwOnCapabilityNotPresent = throwOnCapabilityNotPresent;
    }
}
