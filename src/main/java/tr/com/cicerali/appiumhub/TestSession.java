package tr.com.cicerali.appiumhub;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

public class TestSession {

    private static final Logger logger = LoggerFactory.getLogger(TestSession.class);

    private volatile long sessionCreatedAt;
    private volatile long lastActivity;
    private final StartSessionRequest startSessionRequest;
    private final RemoteNode remoteNode;
    private boolean keepAuthorizationHeaders;
    private String sessionKey;
    private final Map<String, Object> sessionData = new LinkedHashMap<>();
    private volatile boolean stopped = false;
    private boolean forwardingRequest = false;
    private boolean started = false;

    public TestSession(StartSessionRequest startSessionRequest, RemoteNode remoteNode, boolean keepAuthorizationHeaders) {
        this.startSessionRequest = startSessionRequest;
        this.remoteNode = remoteNode;
        this.lastActivity = System.currentTimeMillis();
        this.keepAuthorizationHeaders = keepAuthorizationHeaders;
    }

    public ResponseEntity<byte[]> forwardRegularRequest(RegularSessionRequest sessionRequest) throws IOException, HubSessionException {
        try {
            forwardingRequest = true;
            return forwardRequest(sessionRequest);
        } finally {
            forwardingRequest = false;
        }

    }

    public ResponseEntity<byte[]> forwardNewSessionRequest() throws IOException, HubSessionException {
        try {
            forwardingRequest = true;
            ResponseEntity<byte[]> res = forwardRequest(startSessionRequest);
            started = true;
            return res;
        } finally {
            forwardingRequest = false;
        }
    }

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

    Predicate<String> shouldRemoveHeader = s -> {
        if ("Content-Length".equalsIgnoreCase(s)) {
            return true; // already will set
        }
        if (keepAuthorizationHeaders) {
            return false;
        } else {
            return "Authorization".equalsIgnoreCase(s) || "Proxy-Authorization".equalsIgnoreCase(s);
        }
    };

    private ResponseEntity<byte[]> forwardRequest(WebDriverRequest webDriverRequest) throws IOException, HubSessionException {

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

        if (webDriverRequest.getRequestType() == RequestType.START_SESSION) {
            setSessionKey(responseEntity);
            if (this.sessionKey == null) {
                throw new SessionCreateException("webdriver new session JSON response body did not contain a session Id");
            }
            this.sessionCreatedAt = this.lastActivity;
        }

        return responseEntity;
    }

    private void setSessionKey(ResponseEntity<byte[]> responseEntity) throws IOException {

        byte[] body = responseEntity.getBody();
        if (responseEntity.getStatusCodeValue() == HttpServletResponse.SC_OK) {

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.reader().readTree(body);
            if (root.get("sessionId") == null) {
                root = root.get("value");
            }
            this.sessionKey = root.get("sessionId").textValue();
            this.sessionData.put("id", sessionKey);
            this.sessionData.put("capabilities", objectMapper.convertValue(root.get("capabilities"), new TypeReference<Map<String, Object>>() {
            }));
        }
    }

    private void processResponseHeaders(
            HttpServletRequest request,
            URL remoteURL,
            ResponseEntity<?> responseEntity) throws MalformedURLException {
        HttpHeaders httpHeaders = responseEntity.getHeaders();
        List<String> encoding = httpHeaders.getOrEmpty(HttpHeaders.TRANSFER_ENCODING);
        encoding.forEach(v -> {
            if (v.equalsIgnoreCase("chunked")) {
                httpHeaders.remove(HttpHeaders.TRANSFER_ENCODING);
            }
        });

        List<String> location = httpHeaders.getOrEmpty(HttpHeaders.LOCATION);
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

    public StartSessionRequest getStartSessionRequest() {
        return startSessionRequest;
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
