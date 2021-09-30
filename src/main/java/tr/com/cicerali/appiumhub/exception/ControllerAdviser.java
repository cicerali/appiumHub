package tr.com.cicerali.appiumhub.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
@RestController
public class ControllerAdviser {

    private static final Logger logger = LoggerFactory.getLogger(ControllerAdviser.class);

    @ExceptionHandler(HubRegisterException.class)
    public final ResponseEntity<String> handleHubRegisterException(HubRegisterException ex, WebRequest request) {

        logger.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.toString());
    }

    @ExceptionHandler(HubSessionException.class)
    public final ResponseEntity<String> handleHubSessionException(HubSessionException ex, WebRequest request) {

        logger.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.toString());
    }

    @ExceptionHandler(NodeNotFoundException.class)
    public final ResponseEntity<String> handleNodeNotFoundException(NodeNotFoundException ex, WebRequest request) {

        logger.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.toString());
    }

    @ExceptionHandler(ProxyForwardException.class)
    public final ResponseEntity<String> handleProxyForwardException(ProxyForwardException ex, WebRequest request) {

        logger.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ex.toString());
    }

    @ExceptionHandler(ProxyTimeoutException.class)
    public final ResponseEntity<String> handleProxyTimeoutException(ProxyTimeoutException ex, WebRequest request) {

        logger.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(ex.toString());
    }

    @ExceptionHandler(RequestParseException.class)
    public final ResponseEntity<String> handleRequestParseException(RequestParseException ex, WebRequest request) {

        logger.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.toString());
    }

    @ExceptionHandler(SessionClosedException.class)
    public final ResponseEntity<String> handleSessionClosedException(SessionClosedException ex, WebRequest request) {

        logger.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.toString());
    }

    @ExceptionHandler(SessionCreateException.class)
    public final ResponseEntity<String> handleSessionCreateException(SessionCreateException ex, WebRequest request) {

        logger.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.toString());
    }

    @ExceptionHandler(CapabilityNotFoundException.class)
    public final ResponseEntity<String> handleCapabilityNotFoundException(CapabilityNotFoundException ex, WebRequest request) {

        logger.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.toString());
    }

    @ExceptionHandler(SessionNotFoundException.class)
    public final ResponseEntity<String> handleSessionNotFoundException(SessionNotFoundException ex, WebRequest request) {

        logger.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.toString());
    }
}
