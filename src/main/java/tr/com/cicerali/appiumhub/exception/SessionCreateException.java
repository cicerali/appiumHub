package tr.com.cicerali.appiumhub.exception;

import java.util.Objects;

public class SessionCreateException extends HubSessionException{
    public SessionCreateException(String message, Exception cause) {
        super(message, Objects.requireNonNull(cause));
    }

    public SessionCreateException(Exception cause) {
        super(cause);
    }

    public SessionCreateException(String message) {
        super(message);
    }
}
