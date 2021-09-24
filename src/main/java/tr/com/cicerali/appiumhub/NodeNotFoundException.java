package tr.com.cicerali.appiumhub;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class NodeNotFoundException extends UncheckedHubException{

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
