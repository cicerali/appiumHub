package tr.com.cicerali.appiumhub;

import com.google.common.io.ByteStreams;
import org.apache.commons.lang3.StringUtils;
import tr.com.cicerali.appiumhub.exception.RequestParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

import static tr.com.cicerali.appiumhub.Constants.DEFAULT_BASE_PATH;

/**
 * Provides a base for all session request types
 */
public abstract class WebDriverRequest extends HttpServletRequestWrapper {

    private final RequestType requestType;
    private final String path;
    private final byte[] body;

    /**
     * @param request     original http servlet request
     * @param requestType request type
     * @throws RequestParseException if parsing request body fail
     */
    protected WebDriverRequest(HttpServletRequest request, RequestType requestType) throws RequestParseException {
        super(request);
        this.requestType = requestType;
        this.path = StringUtils.substringAfter(getRequestURI(), DEFAULT_BASE_PATH);
        try {
            this.body = ByteStreams.toByteArray(getInputStream());
        } catch (IOException e) {
            throw new RequestParseException(e);
        }
    }

    /**
     * @return request type
     */
    public RequestType getRequestType() {
        return requestType;
    }

    /**
     * @return request body
     */
    public byte[] getBody() {
        return body;
    }

    /**
     * @return request path without base
     */
    public String getPath() {
        return path;
    }
}
