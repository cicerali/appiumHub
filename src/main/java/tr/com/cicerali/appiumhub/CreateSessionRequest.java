package tr.com.cicerali.appiumhub;

import tr.com.cicerali.appiumhub.exception.RequestParseException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.function.Function;

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
    public CreateSessionRequest(HttpServletRequest request, Function<String, Map<String, Object>> capabilityExtractor) throws RequestParseException {
        super(request, RequestType.START_SESSION);
        try {
            this.desiredCapabilities = capabilityExtractor.apply(new String(getBody()));
        } catch (Exception e) {
            throw new RequestParseException("Could not extract desired capabilities from request", e);
        }
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
