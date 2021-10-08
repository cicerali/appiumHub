package tr.com.cicerali.appiumhub;


import javax.validation.constraints.NotNull;
import java.net.URL;

/**
 * Specifies the node configuration sent by the registration request
 * if one of the parameters is missing, the node registration process
 * uses the default parameter specified by HubConfig
 */
public class NodeConfiguration {

    @NotNull
    private String id;
    private Integer cleanUpCycle;
    private Integer timeout;
    private Integer browserTimeout;
    private Integer nodePolling;
    private Integer nodeStatusCheckTimeout;
    private Integer unregisterIfStillDownAfter;
    private Integer downPollingLimit;

    @NotNull
    private URL url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Integer getBrowserTimeout() {
        return browserTimeout;
    }

    public void setBrowserTimeout(Integer browserTimeout) {
        this.browserTimeout = browserTimeout;
    }

    public Integer getNodeStatusCheckTimeout() {
        return nodeStatusCheckTimeout;
    }

    public void setNodeStatusCheckTimeout(Integer nodeStatusCheckTimeout) {
        this.nodeStatusCheckTimeout = nodeStatusCheckTimeout;
    }

    public Integer getNodePolling() {
        return nodePolling;
    }

    public void setNodePolling(Integer nodePolling) {
        this.nodePolling = nodePolling;
    }

    public Integer getUnregisterIfStillDownAfter() {
        return unregisterIfStillDownAfter;
    }

    public void setUnregisterIfStillDownAfter(Integer unregisterIfStillDownAfter) {
        this.unregisterIfStillDownAfter = unregisterIfStillDownAfter;
    }

    public Integer getDownPollingLimit() {
        return downPollingLimit;
    }

    public void setDownPollingLimit(Integer downPollingLimit) {
        this.downPollingLimit = downPollingLimit;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "NodeConfiguration{" +
                "id='" + id + '\'' +
                ", cleanUpCycle=" + cleanUpCycle +
                ", timeout=" + timeout +
                ", browserTimeout=" + browserTimeout +
                ", nodePolling=" + nodePolling +
                ", nodeStatusCheckTimeout=" + nodeStatusCheckTimeout +
                ", unregisterIfStillDownAfter=" + unregisterIfStillDownAfter +
                ", downPollingLimit=" + downPollingLimit +
                ", url=" + url +
                '}';
    }

    public NodeConfiguration(NodeConfiguration source) {
        this.id = source.id;
        this.cleanUpCycle = source.cleanUpCycle;
        this.timeout = source.timeout;
        this.browserTimeout = source.browserTimeout;
        this.nodePolling = source.downPollingLimit;
        this.nodeStatusCheckTimeout = source.nodeStatusCheckTimeout;
        this.unregisterIfStillDownAfter = source.unregisterIfStillDownAfter;
        this.downPollingLimit = source.downPollingLimit;
        this.url = source.url;
    }

    public NodeConfiguration() {

    }
}
