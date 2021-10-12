package tr.com.cicerali.appiumhub;

import org.json.JSONObject;
import tr.com.cicerali.appiumhub.exception.RequestParseException;

import javax.servlet.http.HttpServletRequest;
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

        /* check JSONWP
         * requiredCapabilities deprecated and removed selenium source code
         * with 3.7.0 788936fa0597d0755185fa89566381d9555bf5a4 */
        try {
            JSONObject root = new JSONObject(new String(getBody()));
            if (root.has("desiredCapabilities")) {
                return root.getJSONObject("desiredCapabilities").toMap();
            }
        } catch (Exception e) {
            throw new RequestParseException("Could not parse request", e);
        }

        /* check W3C */
        try {
            JSONObject root = new JSONObject(new String(getBody()));
            if (root.has("capabilities")) {
                JSONObject aMatch = root.optJSONObject("alwaysMatch");
                JSONObject fMatch = root.optJSONObject("firstMatch");
                Map<String, Object> match;
                if (aMatch != null || fMatch != null) {
                    match = new HashMap<>();
                    if (aMatch != null) {
                        match.putAll(aMatch.toMap());
                    }
                    if (fMatch != null) {
                        match.putAll(fMatch.toMap());
                    }
                    return match;
                }
            }
        } catch (Exception e) {
            throw new RequestParseException("Could not parse request", e);
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
