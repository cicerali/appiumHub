package tr.com.cicerali.appiumhub;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TestSession {
    private volatile long sessionCreatedAt;
    private volatile long lastActivity;
    private final WebDriverRequest webDriverRequest;
    private final RemoteNode remoteNode;
    private String sessionKey;

    private volatile boolean stopped = false;

    public TestSession(WebDriverRequest requestedCapabilities, RemoteNode remoteNode) {
        this.webDriverRequest = requestedCapabilities;
        this.remoteNode = remoteNode;
        this.lastActivity = System.currentTimeMillis();
    }

    public ResponseEntity<?> forwardRequest() throws IOException, HubSessionException {
        return forwardRequest(false);
    }

    public ResponseEntity<?> forwardNewSessionRequest() throws IOException, HubSessionException {
        return forwardRequest(true);
    }

    private ResponseEntity<?> forwardRequest(boolean newSession) throws IOException, HubSessionException {

        this.lastActivity = System.currentTimeMillis();
        RestTemplate restTemplate = remoteNode.getRestTemplate();

        URL remoteURL = remoteNode.getConfiguration().getUrl();
        String pathSpec = webDriverRequest.getServletPath() + webDriverRequest.getContextPath();
        String path = webDriverRequest.getRequestURI();
        if (!path.startsWith(pathSpec)) {
            throw new IllegalStateException(
                    "Expected path " + path + " to start with pathSpec " + pathSpec);
        }
        String end = path.substring(pathSpec.length());
        String ok = remoteURL + end;
        if (webDriverRequest.getQueryString() != null) {
            ok += "?" + webDriverRequest.getQueryString();
        }
        String uri = new URL(remoteURL, ok).toExternalForm();

        lastActivity = System.currentTimeMillis();
        MultiValueMap<String, String> headers = new HttpHeaders();

        for (Enumeration<String> e = webDriverRequest.getHeaderNames(); e.hasMoreElements(); ) {
            String headerName = e.nextElement();
            if ("Content-Length".equalsIgnoreCase(headerName)) {
                continue; // already set
            }
            headers.add(headerName, webDriverRequest.getHeader(headerName));
        }

        HttpEntity<byte[]> entity = new HttpEntity<>(webDriverRequest.getBody(), headers);
        HttpMethod httpMethod = HttpMethod.resolve(webDriverRequest.getMethod());
        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(uri, Objects.requireNonNull(httpMethod), entity, byte[].class);

        lastActivity = System.currentTimeMillis();
        HttpStatus httpStatus = responseEntity.getStatusCode();
        processResponseHeaders(webDriverRequest, remoteURL, responseEntity);

        if (newSession && (!httpStatus.is4xxClientError() || !httpStatus.is5xxServerError())) {
            setSessionKey(responseEntity);
            if (this.sessionKey == null) {
                throw new SessionCreateException(
                        "webdriver new session JSON response body did not contain a session Id");
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
            JsonNode session = root.get("sessionId");
            if (session != null && session.isValueNode()) {
                this.sessionKey = session.textValue();
            } else {
                JsonNode value = root.get("value");
                Map<String, String> map = objectMapper.convertValue(value, new TypeReference<Map<String, String>>() {
                });
                this.sessionKey = map.get("sessionId");
            }
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
}
