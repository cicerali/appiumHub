package tr.com.cicerali.appiumhub;

import org.springframework.http.client.ClientHttpRequestInterceptor;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    public final List<ClientHttpRequestInterceptor> httpRequestInterceptors;
    public final List<TestSessionInterceptor> testSessionInterceptors;

    public static final CapabilityMatcher testMatcher = (currentCapability, requestedCapability) -> true;

    private HubConfig(HubProperties hubProperties, CapabilityMatcher capabilityMatcher, List<ClientHttpRequestInterceptor> httpRequestInterceptors, List<TestSessionInterceptor> testSessionInterceptors) {

        this.pathPrefix = hubProperties.getPathPrefix();
        this.keepAuthorizationHeaders = hubProperties.getKeepAuthorizationHeaders();
        this.stopOnProxyError = hubProperties.getStopOnProxyError();
        this.browserTimeout = hubProperties.getBrowserTimeout();
        this.newSessionWaitTimeout = hubProperties.getNewSessionWaitTimeout();
        this.cleanUpCycle = hubProperties.getCleanUpCycle();
        this.timeout = hubProperties.getTimeout();
        this.nodePolling = hubProperties.getNodePolling();
        this.downPollingLimit = hubProperties.getDownPollingLimit();
        this.unregisterIfStillDownAfter = hubProperties.getUnregisterIfStillDownAfter();
        this.nodeStatusCheckTimeout = hubProperties.getNodeStatusCheckTimeout();
        this.throwOnCapabilityNotPresent = hubProperties.getThrowOnCapabilityNotPresent();
        this.capabilityMatcher = capabilityMatcher;
        this.httpRequestInterceptors = Collections.unmodifiableList(httpRequestInterceptors);
        this.testSessionInterceptors = Collections.unmodifiableList(testSessionInterceptors);
    }

    /**
     * Builder class for HubConfig
     * It will create HubConfig object with default properties
     * if not explicitly specified
     */
    public static class HubConfigBuilder {
        private final HubProperties props;
        private CapabilityMatcher capabilityMatcher;
        private final List<ClientHttpRequestInterceptor> httpRequestInterceptors = new ArrayList<>();
        private final List<TestSessionInterceptor> testSessionInterceptors = new ArrayList<>();

        private HubConfigBuilder(HubProperties props) {
            this.props = props;
        }

        public static HubConfigBuilder fromHubProperties(@NotNull HubProperties hubProperties) {

            HubConfigBuilder builder = new HubConfigBuilder(hubProperties);

            builder.props.setPathPrefix(hubProperties.getPathPrefix());
            builder.props.setBrowserTimeout(hubProperties.getBrowserTimeout());
            builder.props.setCleanUpCycle(hubProperties.getCleanUpCycle());
            builder.props.setDownPollingLimit(hubProperties.getDownPollingLimit());
            builder.props.setNodePolling(hubProperties.getNodePolling());
            builder.props.setKeepAuthorizationHeaders(hubProperties.getKeepAuthorizationHeaders());
            builder.props.setNewSessionWaitTimeout(hubProperties.getNewSessionWaitTimeout());
            builder.props.setNodeStatusCheckTimeout(hubProperties.getNodeStatusCheckTimeout());
            builder.props.setStopOnProxyError(hubProperties.getStopOnProxyError());
            builder.props.setThrowOnCapabilityNotPresent(hubProperties.getThrowOnCapabilityNotPresent());
            builder.props.setTimeout(hubProperties.getTimeout());
            builder.props.setUnregisterIfStillDownAfter(hubProperties.getUnregisterIfStillDownAfter());
            return builder;
        }

        public static HubConfigBuilder fromScratch() {
            return new HubConfigBuilder(new HubProperties());
        }

        public HubConfig build() {

            if (capabilityMatcher == null) {
                capabilityMatcher = new DefaultCapabilityMatcher(true);
            }

            if (props.getPathPrefix() == null) {
                props.setPathPrefix(DEFAULT_PATH_PREFIX);
            }

            if (props.getKeepAuthorizationHeaders() == null) {
                props.setKeepAuthorizationHeaders(DEFAULT_KEEP_AUTHORIZATION_HEADERS);
            }

            if (props.getStopOnProxyError() == null) {
                props.setStopOnProxyError(DEFAULT_STOP_ON_PROXY_ERROR);
            }

            if (props.getBrowserTimeout() == null) {
                props.setBrowserTimeout(DEFAULT_BROWSER_TIMEOUT);
            }

            if (props.getNewSessionWaitTimeout() == null) {
                props.setNewSessionWaitTimeout(DEFAULT_NEW_SESSION_WAIT_TIMEOUT);
            }

            if (props.getCleanUpCycle() == null) {
                props.setCleanUpCycle(DEFAULT_CLEAN_UP_CYCLE_INTERVAL);
            }

            if (props.getTimeout() == null) {
                props.setTimeout(DEFAULT_SESSION_TIMEOUT);
            }

            if (props.getNodePolling() == null) {
                props.setNodePolling(DEFAULT_POLLING_INTERVAL);
            }

            if (props.getDownPollingLimit() == null) {
                props.setDownPollingLimit(DEFAULT_DOWN_POLLING_LIMIT);
            }

            if (props.getUnregisterIfStillDownAfter() == null) {
                props.setUnregisterIfStillDownAfter(DEFAULT_UNREGISTER_DELAY);
            }

            if (props.getNodeStatusCheckTimeout() == null) {
                props.setNodeStatusCheckTimeout(DEFAULT_NODE_STATUS_CHECK_TIMEOUT);
            }

            if (props.getThrowOnCapabilityNotPresent() == null) {
                props.setThrowOnCapabilityNotPresent(DEFAULT_THROW_ON_CAPABILITY_NOT_PRESENT);
            }

            return new HubConfig(props, capabilityMatcher, httpRequestInterceptors, testSessionInterceptors);
        }

        public HubConfigBuilder setCapabilityMatcher(CapabilityMatcher matcher) {
            capabilityMatcher = matcher;
            return this;
        }

        public HubConfigBuilder addHttpRequestInterceptor(ClientHttpRequestInterceptor interceptor) {
            httpRequestInterceptors.add(interceptor);
            return this;
        }

        public HubConfigBuilder addTestSessionInterceptor(TestSessionInterceptor testSessionInterceptor) {
            testSessionInterceptors.add(testSessionInterceptor);
            return this;
        }

        public HubConfigBuilder setPathPrefix(String pathPrefix) {
            props.setPathPrefix(pathPrefix);
            return this;
        }

        public HubConfigBuilder setBrowserTimeout(int browserTimeout) {
            props.setBrowserTimeout(browserTimeout);
            return this;
        }

        public HubConfigBuilder setCleanUpCycle(int cleanUpCycle) {
            props.setCleanUpCycle(cleanUpCycle);
            return this;
        }

        public HubConfigBuilder setDownPollingLimit(int downPollingLimit) {
            props.setDownPollingLimit(downPollingLimit);
            return this;
        }

        public HubConfigBuilder setNodePolling(int nodePolling) {
            props.setNodePolling(nodePolling);
            return this;
        }

        public HubConfigBuilder setKeepAuthorizationHeaders(boolean keepAuthorizationHeaders) {
            props.setKeepAuthorizationHeaders(keepAuthorizationHeaders);
            return this;
        }

        public HubConfigBuilder setNewSessionWaitTimeout(int newSessionWaitTimeout) {
            props.setNewSessionWaitTimeout(newSessionWaitTimeout);
            return this;
        }

        public HubConfigBuilder setNodeStatusCheckTimeout(int nodeStatusCheckTimeout) {
            props.setNodeStatusCheckTimeout(nodeStatusCheckTimeout);
            return this;
        }

        public HubConfigBuilder setStopOnProxyError(boolean stopOnProxyError) {
            props.setStopOnProxyError(stopOnProxyError);
            return this;
        }

        public HubConfigBuilder setThrowOnCapabilityNotPresent(boolean throwOnCapabilityNotPresent) {
            props.setThrowOnCapabilityNotPresent(throwOnCapabilityNotPresent);
            return this;
        }

        public HubConfigBuilder setTimeout(int timeout) {
            props.setTimeout(timeout);
            return this;
        }

        public HubConfigBuilder setUnregisterIfStillDownAfter(int unregisterIfStillDownAfter) {
            props.setUnregisterIfStillDownAfter(unregisterIfStillDownAfter);
            return this;
        }
    }
}
