package tr.com.cicerali.appiumhub;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tr.com.cicerali.appiumhub.exception.HubSessionException;
import tr.com.cicerali.appiumhub.exception.SessionCreateException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;

/**
 * Specifies a test session triggered by an appium test client.
 * Constructed with new session request and live until delete session request
 * or when inactivity timeout expired.
 * It forwards all client request to remote node.
 */
public class TestSession {

    private static final Logger logger = LoggerFactory.getLogger(TestSession.class);

    private final CreateSessionRequest createSessionRequest;
    private final RemoteNode remoteNode;
    private final Map<String, Object> sessionData = new LinkedHashMap<>();
    private final Predicate<String> shouldRemoveHeader;

    private volatile long sessionCreatedAt;
    private volatile long lastActivity;
    private String sessionKey;
    private volatile boolean stopped = false;
    private volatile boolean forwardingRequest = false;
    private volatile boolean started = false;

    /**
     * @param createSessionRequest     new session request
     * @param remoteNode               remote node which requests will be forwarded
     * @param keepAuthorizationHeaders {@code true} clients' authorization headers will be kept in requests
     */
    public TestSession(CreateSessionRequest createSessionRequest, RemoteNode remoteNode, boolean keepAuthorizationHeaders) {
        this.createSessionRequest = createSessionRequest;
        this.remoteNode = remoteNode;
        this.lastActivity = System.currentTimeMillis();
        this.shouldRemoveHeader = s -> "Content-Length".equalsIgnoreCase(s) ||
                (!keepAuthorizationHeaders && ("Authorization".equalsIgnoreCase(s) || "Proxy-Authorization".equalsIgnoreCase(s)));
    }

    /**
     * @param sessionRequest session related regular request
     * @return remote node response
     * @throws IOException if proxying fail
     */
    public ResponseEntity<byte[]> forwardRegularRequest(RegularSessionRequest sessionRequest) throws IOException {
        try {
            forwardingRequest = true;
            return forwardRequest(sessionRequest);
        } finally {
            forwardingRequest = false;
        }
    }

    /**
     * @return new session response from remote node
     * @throws IOException         if proxying fail
     * @throws HubSessionException if response not as expected
     */
    public ResponseEntity<byte[]> forwardNewSessionRequest() throws IOException, HubSessionException {
        try {
            forwardingRequest = true;
            ResponseEntity<byte[]> res = forwardRequest(createSessionRequest);
            setSessionKeyAndData(res);
            if (this.sessionKey == null) {
                throw new SessionCreateException("Proxy response does not contain session id");
            }
            this.sessionCreatedAt = this.lastActivity;
            started = true;
            return res;
        } finally {
            forwardingRequest = false;
        }
    }

    /**
     * deletes the session on the remote node, if any
     */
    public void deleteSession() {
        if (!started || stopped) {
            return;
        }
        RestTemplate restTemplate = remoteNode.getRestTemplate();
        URL remoteURL = remoteNode.getConfiguration().getUrl();
        String ok = remoteURL + "/session/" + sessionKey;
        try {
            String uri = new URL(remoteURL, ok).toExternalForm();
            restTemplate.delete(uri);
        } catch (Exception e) {
            logger.error("Causing an error during session delete: {}", e.getMessage());
        }
    }

    private ResponseEntity<byte[]> forwardRequest(WebDriverRequest webDriverRequest) throws IOException {

        this.lastActivity = System.currentTimeMillis();
        RestTemplate restTemplate = remoteNode.getRestTemplate();

        URL remoteURL = remoteNode.getConfiguration().getUrl();
        String end = webDriverRequest.getPath();
        String ok = remoteURL + end;
        if (webDriverRequest.getQueryString() != null) {
            ok += "?" + webDriverRequest.getQueryString();
        }
        String uri = new URL(remoteURL, ok).toExternalForm();

        this.lastActivity = System.currentTimeMillis();
        MultiValueMap<String, String> headers = new HttpHeaders();

        for (Enumeration<String> e = webDriverRequest.getHeaderNames(); e.hasMoreElements(); ) {
            String headerName = e.nextElement();
            if (shouldRemoveHeader.test(headerName)) {
                continue;
            }
            headers.add(headerName, webDriverRequest.getHeader(headerName));
        }

        HttpEntity<byte[]> entity = new HttpEntity<>(webDriverRequest.getBody(), headers);
        HttpMethod httpMethod = HttpMethod.resolve(webDriverRequest.getMethod());
        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(uri, Objects.requireNonNull(httpMethod), entity, byte[].class);

        this.lastActivity = System.currentTimeMillis();
        processResponseHeaders(webDriverRequest, remoteURL, responseEntity);
        return responseEntity;
    }

    private void setSessionKeyAndData(ResponseEntity<byte[]> responseEntity) {

        byte[] body = responseEntity.getBody();
        if (responseEntity.getStatusCodeValue() == HttpServletResponse.SC_OK && body != null) {

            JSONObject root = new JSONObject(new String(body));
            if (root.has("sessionId")) {
                this.sessionData.put("caps", root.getJSONObject("value").toMap());
            } else {
                root = root.getJSONObject("value");
                this.sessionData.put("caps", root.getJSONObject("capabilities").toMap());
            }
            this.sessionKey = root.optString("sessionId");
            this.sessionData.put("id", sessionKey);
        }
    }

    private void processResponseHeaders(
            HttpServletRequest request,
            URL remoteURL,
            ResponseEntity<?> responseEntity) throws MalformedURLException {
        HttpHeaders httpHeaders = responseEntity.getHeaders();
        List<String> encoding = httpHeaders.getOrDefault(HttpHeaders.TRANSFER_ENCODING, Collections.emptyList());
        encoding.forEach(v -> {
            if (v.equalsIgnoreCase("chunked")) {
                httpHeaders.remove(HttpHeaders.TRANSFER_ENCODING);
            }
        });

        List<String> location = httpHeaders.getOrDefault(HttpHeaders.LOCATION, Collections.emptyList());
        if (!location.isEmpty()) {
            URL returnedLocation = new URL(remoteURL, location.get(0));
            String driverPath = remoteURL.getPath();
            String wrongPath = returnedLocation.getPath();
            String correctPath = wrongPath.replace(driverPath, "");
            URL url = new URL(request.getRequestURL().toString());
            String pathSpec = request.getServletPath() + request.getContextPath();
            httpHeaders.setLocation(URI.create(url.getProtocol() + "://" + url.getHost() + url.getPort() + pathSpec + correctPath));
        }
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public long getSessionCreatedAt() {
        return sessionCreatedAt;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public RemoteNode getRemoteNode() {
        return remoteNode;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public CreateSessionRequest getStartSessionRequest() {
        return createSessionRequest;
    }

    public Map<String, Object> getSessionData() {
        return sessionData;
    }

    public long getInactivityTime() {
        return System.currentTimeMillis() - lastActivity;
    }

    public boolean isForwardingRequest() {
        return forwardingRequest;
    }
}
