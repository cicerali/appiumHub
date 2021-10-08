package tr.com.cicerali.appiumhub;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import tr.com.cicerali.appiumhub.exception.RequestParseException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Specifies new session request
 */
public class CreateSessionRequest extends WebDriverRequest {

    private final Map<String, Object> desiredCapabilities;

    /**
     * Represents a new session request object, and It will determine
     * the desired capabilities from the original request body.
     * Client can send it in different forms, so we need to make it suitable
     *
     * @param request original http servlet request
     * @throws RequestParseException if extracting desired capabilities fail
     */
    public CreateSessionRequest(HttpServletRequest request) throws RequestParseException {
        super(request, RequestType.START_SESSION);
        this.desiredCapabilities = extractDesiredCapabilities();
    }

    private Map<String, Object> extractDesiredCapabilities() throws RequestParseException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root;
        try {
            root = objectMapper.reader().readTree(new String(getBody()));
        } catch (IOException e) {
            throw new RequestParseException("Could not parse request", e);
        }

        /* check JSONWP */
        JsonNode desiredCaps = root.get("desiredCapabilities");
        if (desiredCaps != null) {
            return objectMapper.convertValue(desiredCaps, new TypeReference<Map<String, Object>>() {
            });
        }

        /* check W3C*/
        JsonNode capabilities = root.get("capabilities");
        if (capabilities != null) {
            JsonNode aMatch = capabilities.get("alwaysMatch");
            JsonNode fMatch = capabilities.get("firstMatch");

            Map<String, Object> match;
            if (aMatch != null || fMatch != null) {
                match = new HashMap<>();
                if (aMatch != null) {
                    match.putAll(objectMapper.convertValue(aMatch, new TypeReference<Map<String, Object>>() {
                    }));

                    if (fMatch != null) {
                        match.putAll(objectMapper.convertValue(aMatch, new TypeReference<Map<String, Object>>() {
                        }));
                    }
                }
                return match;
            }
        }

        throw new RequestParseException("Could not found desired capabilities in request");
    }

    /**
     * It will return clients desired capabilities which extracted from request body
     *
     * @return desired capabilities
     */
    public Map<String, Object> getDesiredCapabilities() {
        return desiredCapabilities;
    }
}
