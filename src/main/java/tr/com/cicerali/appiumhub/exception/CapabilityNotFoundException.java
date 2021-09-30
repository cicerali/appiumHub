package tr.com.cicerali.appiumhub.exception;

public class CapabilityNotFoundException extends HubSessionException {

    public CapabilityNotFoundException(String message, Exception cause) {
        super(message, cause);
    }

    public CapabilityNotFoundException(Exception cause) {
        super(cause);
    }

    public CapabilityNotFoundException(String message) {
        super(message);
    }
}
