package tr.com.cicerali.appiumhub;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Map;

public class WebDriverRequest extends HttpServletRequestWrapper {

    private final RequestType requestType;
    private byte[] body;
    private String sessionKey;
    private final Map<String, Object> desiredCapabilities;

    public WebDriverRequest(HttpServletRequest request) throws HubSessionException {
        super(request);
        String path = StringUtils.defaultString(request.getPathInfo());
        if (!path.startsWith("/session")) {
            throw new HubSessionException("No matching session endpoint");
        }

        if (path.equals("/session")) {
            this.requestType = RequestType.START_SESSION;
        } else {
            sessionKey = getSessionKey(path);
            if (sessionKey != null && request.getMethod().equals(HttpMethod.DELETE.name()) && path.endsWith("/session/" + sessionKey)) {
                this.requestType = RequestType.DELETE_SESSION;
            } else {
                this.requestType = RequestType.REGULAR;
            }
        }
        if (requestType == RequestType.START_SESSION) {
            try {
                desiredCapabilities = extractDesiredCapabilities();
            } catch (Exception e) {
                throw new HubSessionException(e);
            }
        } else {
            desiredCapabilities = null;
        }

        try {
            setBody(IOUtils.toByteArray(super.getInputStream()));
        } catch (IOException e) {
            throw new HubSessionException(e);
        }
    }

    private String getSessionKey(String path) {
        if (path.startsWith("/session/")) {
            String[] splitPath = StringUtils.split(path, '/');
            return StringUtils.isEmpty(splitPath[2]) ? null : splitPath[2];
        }
        return null;
    }

    private Map<String, Object> extractDesiredCapabilities() throws IOException {


        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.reader().readTree(body);

        /* check JSONWP */
        JsonNode desiredCaps = root.get("desiredCapabilities");
        if (desiredCaps != null) {
            return objectMapper.convertValue(desiredCaps, new TypeReference<Map<String, Object>>() {
            });
        }

        /* check W3C*/
        JsonNode capabilities = root.get("capabilities");
        if (capabilities != null) {
            JsonNode match = root.get("alwaysMatch");
            Map<String, Object> alwaysMatch = objectMapper.convertValue(match, new TypeReference<Map<String, Object>>() {
            });
            match = root.get("firstMatch");
            alwaysMatch.putAll(objectMapper.convertValue(match, new TypeReference<Map<String, Object>>() {
            }));

            return alwaysMatch;
        }

        return null;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public Map<String, Object> getDesiredCapabilities() {
        return desiredCapabilities;
    }
}
