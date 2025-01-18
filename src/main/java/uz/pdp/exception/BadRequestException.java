package uz.pdp.exception;

import java.util.Map;
import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    private Map<String, String> errors;

    public BadRequestException(String message) {
        super(message);
    }
    
    public BadRequestException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }

    public BadRequestException(String message, String errorDetail, Throwable cause) {
        super(message, cause);
        this.errors = new HashMap<>();
        this.errors.put("error", errorDetail);
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
