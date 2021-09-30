package tr.com.cicerali.appiumhub;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import tr.com.cicerali.appiumhub.exception.RequestParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Optional;

public abstract class WebDriverRequest extends HttpServletRequestWrapper {

    public static final String BASE_PATH = Optional.of("/wd/hub").get();
    protected RequestType requestType = RequestType.REGULAR;
    private final String path;
    protected byte[] body;

    protected WebDriverRequest(HttpServletRequest request) throws RequestParseException {
        super(request);
        this.path = findPathInfo();
        readBody();
    }

    public String findPathInfo() {
        return StringUtils.substringAfter(super.getRequestURI(), BASE_PATH);
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    private void readBody() throws RequestParseException {
        try {
            setBody(IOUtils.toByteArray(super.getInputStream()));
        } catch (IOException e) {
            throw new RequestParseException(e);
        }
    }

    public String getPath() {
        return path;
    }
}
