package tr.com.cicerali.appiumhub;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.math.IntMath;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class RemoteNode {

    private static final Logger logger = LoggerFactory.getLogger(RemoteNode.class);

    private final RegistrationRequest registrationRequest;
    private final Map<String, Object> capabilities;
    private NodeConfiguration configuration;
    private final RestTemplate restTemplate;

    public final ReentrantLock lock = new ReentrantLock();
    private volatile boolean isBusy = false;
    private int useCount = 0;
    private long totalUsed = 0;
    private long lastSessionStart = -1;
    private long registerTime = System.currentTimeMillis();
    private final CapabilityMatcher capabilityMatcher;
    private final HubCore hubCore;
    private final String id;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private volatile boolean down = false;

    private final NodeControl nodeControl;

    private TestSession testSession;

    public RemoteNode(RegistrationRequest registrationRequest, CapabilityMatcher capabilityMatcher, HubCore hubCore) throws GeneralSecurityException {
        this.registrationRequest = registrationRequest;
        this.capabilityMatcher = capabilityMatcher;
        Map<String, Object> mergedCaps = new HashMap<>();
        registrationRequest.getCapabilities().forEach(mergedCaps::putAll);
        this.capabilities = mergedCaps;
        this.hubCore = hubCore;
        setConfiguration();
        int timeout = configuration.getBrowserTimeout();
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();

        HttpClient httpClient;
        if (registrationRequest.getConfiguration().getUrl().getProtocol().equals("https")) {
            TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            SSLConnectionSocketFactory scsf = new SSLConnectionSocketFactory(sslContext,
                    NoopHostnameVerifier.INSTANCE);
            httpClient = HttpClientBuilder.create().setSSLSocketFactory(scsf).setDefaultRequestConfig(config).build();
            HttpComponentsClientHttpRequestFactory requestFactory =
                    new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            this.restTemplate = new RestTemplate(requestFactory);
        } else {
            httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
            HttpComponentsClientHttpRequestFactory requestFactory =
                    new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            this.restTemplate = new RestTemplate(requestFactory);
        }
        this.id = configuration.getId();
        this.nodeControl = new NodeControl();
    }

    private void setConfiguration() {

        this.configuration = new NodeConfiguration(registrationRequest.getConfiguration());
        if (configuration.getId() == null) {
            configuration.setId(configuration.getUrl().toString());
        }
        if (configuration.getCleanUpCycle() == null) {
            configuration.setCleanUpCycle(hubCore.getHubConfig().cleanUpCycle);
        }

        if (configuration.getTimeout() == null) {
            configuration.setTimeout(hubCore.getHubConfig().timeout);
        }

        if (configuration.getBrowserTimeout() == null) {
            configuration.setBrowserTimeout(hubCore.getHubConfig().browserTimeout);
        }

        if (configuration.getNodePolling() == null) {
            configuration.setNodePolling(hubCore.getHubConfig().nodePolling);
        }

        if (configuration.getNodeStatusCheckTimeout() == null) {
            configuration.setNodeStatusCheckTimeout(hubCore.getHubConfig().nodeStatusCheckTimeout);
        }

        if (configuration.getUnregisterIfStillDownAfter() == null) {
            configuration.setUnregisterIfStillDownAfter(hubCore.getHubConfig().unregisterIfStillDownAfter);
        }

        if (configuration.getDownPollingLimit() == null) {
            configuration.setDownPollingLimit(hubCore.getHubConfig().downPollingLimit);
        }
    }

    public RegistrationRequest getOriginalRegistrationRequest() {
        return registrationRequest;
    }

    public boolean hasCapability(Map<String, Object> requestedCapability) {
        return capabilityMatcher.matches(capabilities, requestedCapability);
    }

    public double getResourceUsageInPercent() {
        long registered = System.currentTimeMillis() - registerTime;
        return 100 * (double) totalUsed / registered;
    }

    public boolean isBusy() {
        return isBusy;
    }

    public void setBusy(boolean busy) {
        isBusy = busy;
    }

    public boolean isDown() {
        return down;
    }

    public void setDown(boolean down) {
        this.down = down;
    }

    public int getUseCount() {
        return useCount;
    }

    public void setUseCount(int useCount) {
        this.useCount = useCount;
    }

    public long getTotalUsed() {
        return totalUsed;
    }

    public void setTotalUsed(long totalUsed) {
        this.totalUsed = totalUsed;
    }

    public void setLastSessionStart(long lastSessionStart) {
        this.lastSessionStart = lastSessionStart;
    }

    public long getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(long registerTime) {
        this.registerTime = registerTime;
    }

    public long getLastSessionStart() {
        return lastSessionStart;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public NodeConfiguration getConfiguration() {
        return configuration;
    }

    public TestSession getTestSession() {
        return this.testSession;
    }

    public void setTestSession(TestSession testSession) {
        this.testSession = testSession;
    }

    public String getId() {
        return id;
    }

    public Map<String, Object> getCapabilities() {
        return capabilities;
    }

    public void clean() {

        /* clean test session info */
        testSession.deleteSession();
        testSession = null;
        setBusy(false);
    }

    public void destroy(SessionTerminationReason reason) {

        /* stopping node control thread */
        nodeControl.interrupt();
        hubCore.remove(this);
        if (testSession != null) {
            hubCore.cleanSession(testSession, reason);
        }
    }

    public boolean isReachable() {
        try {
            getProxyStatus();
            return true;
        } catch (Exception e) {
            logger.error("Failed to check status of node: {}", e.getMessage());
            return false;
        }
    }

    public Map<String, Object> getProxyStatus() throws MalformedURLException {

        URL remoteURL = configuration.getUrl();
        String end = "/status";
        String ok = remoteURL + end;
        String uri = new URL(remoteURL, ok).toExternalForm();

        JsonNode root = restTemplate.getForObject(uri, JsonNode.class);

        return objectMapper.convertValue(root, new TypeReference<Map<String, Object>>() {
        });
    }

    private class NodeControl extends Thread {

        int failedPollingTries = 0;
        long downSince = 0;

        public NodeControl() {
            super("nodeControl-" + id);
            start();
        }

        private void cleanUpNode() {

            if (testSession != null) {
                long inactivity = testSession.getInactivityTime();
                boolean hasTimedOut = inactivity > configuration.getTimeout() * 1000;
                if (hasTimedOut && !testSession.isForwardingRequest()) {
                    logger.warn("session {} has TIMED OUT due to client inactivity and will be released.", testSession.getSessionKey());
                    hubCore.cleanSession(testSession, SessionTerminationReason.TIMEOUT);
                    hubCore.wakeUpWaiters();
                }
            }
        }

        private void poolNode() {
            if (!isReachable()) {
                if (!down) {
                    failedPollingTries++;
                    if (failedPollingTries >= configuration.getDownPollingLimit()) {
                        downSince = System.currentTimeMillis();
                        logger.info("Marking the node {} as down: cannot reach the node for {} tries", id, failedPollingTries);
                        down = true;
                    }
                } else {
                    long downFor = System.currentTimeMillis() - downSince;
                    if (downFor > configuration.getUnregisterIfStillDownAfter()) {
                        logger.info("Unregistering the node {} because it's been down for {} milliseconds", id, downFor);
                        destroy(SessionTerminationReason.NODE_UNREACHABLE);
                    }
                }
            } else {
                down = false;
                failedPollingTries = 0;
                downSince = 0;
            }
        }

        @Override
        public void run() {
            int eps = Math.max(IntMath.gcd(configuration.getNodePolling(), configuration.getCleanUpCycle()), 500);
            int pollingTime = configuration.getNodePolling();
            int cleanUpTime = configuration.getCleanUpCycle();

            long poolElapsed = 0;
            long cleanElapsed = 0;
            try {
                for (; ; ) {
                    Thread.sleep(eps);

                    poolElapsed += eps;
                    cleanElapsed += eps;
                    if (poolElapsed >= pollingTime) {
                        poolElapsed = 0;
                        poolNode();
                    }
                    if (cleanElapsed >= cleanUpTime) {
                        cleanElapsed = 0;
                        cleanUpNode();
                    }
                }
            } catch (InterruptedException e) {
                logger.info("Node control thread interrupted");
                Thread.currentThread().interrupt();
            }
        }
    }
}
