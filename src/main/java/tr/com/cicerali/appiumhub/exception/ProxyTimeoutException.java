package tr.com.cicerali.appiumhub.exception;

import java.util.Objects;

public class ProxyTimeoutException extends HubSessionException {
    public ProxyTimeoutException(String message, Exception cause) {
        super(message, Objects.requireNonNull(cause));
    }

    public ProxyTimeoutException(Exception cause) {
        super(cause);
    }

    public ProxyTimeoutException(String message) {
        super(message);
    }
}
