package tr.com.cicerali.appiumhub.exception;

import java.util.Objects;

public class RequestParseException extends HubSessionException {
    public RequestParseException(String message, Exception cause) {
        super(message, Objects.requireNonNull(cause));
    }

    public RequestParseException(Exception cause) {
        super(cause);
    }

    public RequestParseException(String message) {
        super(message);
    }
}
