package tr.com.cicerali.appiumhub;


import org.springframework.beans.BeanUtils;

import javax.validation.constraints.NotNull;
import java.net.URL;

public class NodeConfiguration {

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
                ", url=" + url +
                '}';
    }

    NodeConfiguration(NodeConfiguration source) {
        BeanUtils.copyProperties(source, this);
    }

    NodeConfiguration() {

    }
}
