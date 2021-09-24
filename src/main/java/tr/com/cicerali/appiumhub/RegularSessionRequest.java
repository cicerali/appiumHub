package tr.com.cicerali.appiumhub;

import javax.servlet.http.HttpServletRequest;

public class RegularSessionRequest extends WebDriverRequest {

    private final String sessionKey;

    public RegularSessionRequest(HttpServletRequest request, String sessionKey) throws HubSessionException {
        super(request);
        this.requestType = RequestType.REGULAR;
        this.sessionKey = sessionKey;
    }

    public RegularSessionRequest(HttpServletRequest request, String sessionKey, RequestType requestType) throws HubSessionException {
        this(request, sessionKey);
        this.requestType = requestType;
    }

    public String getSessionKey() {
        return sessionKey;
    }
}
