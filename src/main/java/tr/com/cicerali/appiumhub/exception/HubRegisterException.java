package tr.com.cicerali.appiumhub.exception;

public class HubRegisterException extends Exception {

    public HubRegisterException(String message, Throwable cause) {
        super(message, cause);
    }

    public HubRegisterException(Throwable cause) {
        super(cause);
    }

    public HubRegisterException(String message) {
        super(message);
    }
}
