package tr.com.cicerali.appiumhub;

import java.util.Objects;

public class UncheckedHubException extends RuntimeException {

    public UncheckedHubException(String message) {
        super(message);
    }

    public UncheckedHubException(String message, Exception cause) {
        super(message, Objects.requireNonNull(cause));
    }

    public UncheckedHubException(Exception cause) {
        super(Objects.requireNonNull(cause));
    }
}
