package tr.com.cicerali.appiumhub;

import tr.com.cicerali.appiumhub.exception.RequestParseException;

import javax.servlet.http.HttpServletRequest;

/**
 * Determines all session related requests except create new session request
 */
public class RegularSessionRequest extends WebDriverRequest {

    private final String sessionKey;

    /**
     * Represents a session request object except new session request
     *
     * @param request    original http request
     * @param sessionKey session id
     * @throws RequestParseException if parsing request body fail
     */
    public RegularSessionRequest(HttpServletRequest request, String sessionKey) throws RequestParseException {
        super(request, RequestType.REGULAR);
        this.sessionKey = sessionKey;
    }

    /**
     * Represents a session request object except new session request
     *
     * @param request     original http request
     * @param sessionKey  session id
     * @param requestType request type
     * @throws RequestParseException if parsing request body fail
     */
    public RegularSessionRequest(HttpServletRequest request, String sessionKey, RequestType requestType) throws RequestParseException {
        super(request, requestType);
        this.sessionKey = sessionKey;
    }

    /**
     *
     * @return session id
     */
    public String getSessionKey() {
        return sessionKey;
    }
}
