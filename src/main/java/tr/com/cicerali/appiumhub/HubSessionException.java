package tr.com.cicerali.appiumhub;

import java.util.Objects;

public class HubSessionException extends Exception {

    public HubSessionException(String message, Exception cause) {
        super(message, Objects.requireNonNull(cause));
    }

    public HubSessionException(Exception cause) {
        super(Objects.requireNonNull(cause));
    }

    public HubSessionException(String message) {
        super(message);
    }
}
