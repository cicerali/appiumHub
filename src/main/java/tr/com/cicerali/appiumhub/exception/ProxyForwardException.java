package tr.com.cicerali.appiumhub.exception;

import java.util.Objects;

public class ProxyForwardException extends HubSessionException {
    public ProxyForwardException(String message, Exception cause) {
        super(message, Objects.requireNonNull(cause));
    }

    public ProxyForwardException(Exception cause) {
        super(cause);
    }

    public ProxyForwardException(String message) {
        super(message);
    }
}
