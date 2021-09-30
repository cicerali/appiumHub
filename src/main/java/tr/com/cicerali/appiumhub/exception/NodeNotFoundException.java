package tr.com.cicerali.appiumhub.exception;

public class NodeNotFoundException extends UncheckedHubException {

    public NodeNotFoundException(String message) {
        super(message);
    }

    public NodeNotFoundException(String message, Exception cause) {
        super(message, cause);
    }

    public NodeNotFoundException(Exception cause) {
        super(cause);
    }
}
