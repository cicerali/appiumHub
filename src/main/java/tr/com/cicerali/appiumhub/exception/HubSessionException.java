package tr.com.cicerali.appiumhub.exception;

import java.util.Objects;

public abstract class HubSessionException extends Exception {

    protected HubSessionException(String message, Exception cause) {
        super(message, Objects.requireNonNull(cause));
    }

    protected HubSessionException(Exception cause) {
        super(Objects.requireNonNull(cause));
    }

    protected HubSessionException(String message) {
        super(message);
    }
}
