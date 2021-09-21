package tr.com.cicerali.appiumhub;

import java.util.Objects;

public class HubRegisterException extends Exception {

    public HubRegisterException(String message, Exception cause) {
        super(message, Objects.requireNonNull(cause));
    }

    public HubRegisterException(Exception cause) {
        super(Objects.requireNonNull(cause));
    }

    public HubRegisterException(String message) {
        super(message);
    }
}
