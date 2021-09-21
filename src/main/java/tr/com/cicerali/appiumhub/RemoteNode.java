package tr.com.cicerali.appiumhub;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class RemoteNode {

    private final RegistrationRequest registrationRequest;
    private final Map<String, Object> capabilities;
    private final NodeConfiguration configuration;
    private final RestTemplate restTemplate;
    private final HttpClient httpClient;

    public final ReentrantLock lock = new ReentrantLock();
    private volatile boolean isBusy = false;
    private int useCount = 0;
    private long totalUsed = 0;
    private long lastSessionStart = -1;
    private long registerTime = System.currentTimeMillis();
    private final CapabilityMatcher capabilityMatcher;
    private final HubCore hubCore;
    private final String id;

    private final NodeControl nodeControl;
    private TestSession testSession;

    public RemoteNode(RegistrationRequest registrationRequest, CapabilityMatcher capabilityMatcher, HubCore hubCore) throws GeneralSecurityException {
        this.registrationRequest = registrationRequest;
        this.capabilityMatcher = capabilityMatcher;
        Map<String, Object> mergedCaps = new HashMap<>();
        registrationRequest.getCapabilities().forEach(mergedCaps::putAll);
        this.capabilities = mergedCaps;
        this.configuration = registrationRequest.getConfiguration();
        this.hubCore = hubCore;
        int timeout = (configuration.getTimeout() == null) ? hubCore.getHubConfig().browserTimeout : configuration.getTimeout();
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout).build();

        if (registrationRequest.getConfiguration().getUrl().getProtocol().equals("https")) {
            TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            SSLConnectionSocketFactory scsf = new SSLConnectionSocketFactory(sslContext,
                    NoopHostnameVerifier.INSTANCE);
            this.httpClient = HttpClientBuilder.create().setSSLSocketFactory(scsf).setDefaultRequestConfig(config).build();
            HttpComponentsClientHttpRequestFactory requestFactory =
                    new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            this.restTemplate = new RestTemplate(requestFactory);
        } else {
            this.httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
            HttpComponentsClientHttpRequestFactory requestFactory =
                    new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            this.restTemplate = new RestTemplate(requestFactory);
        }
        this.id = configuration.getId();
        this.nodeControl = new NodeControl();
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

    public void clean() {
        // TODO other things
        this.nodeControl.interrupt();
    }

    public void destroy() {
        clean();

    }

    private class NodeControl extends Thread {
        public NodeControl() {
            super("nodeControl-" + id);
        }

        @Override
        public void run() {
// TODO
            // sessionda uzun suredir islem yok
            SessionTerminationReason reason = SessionTerminationReason.TIMEOUT;
            //
        }
    }
}
