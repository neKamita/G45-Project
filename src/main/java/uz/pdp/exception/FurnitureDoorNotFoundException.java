package uz.pdp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a furniture door cannot be found.
 * Because sometimes doors just don't want to be found! 🚪🔍
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class FurnitureDoorNotFoundException extends RuntimeException {
    
    public FurnitureDoorNotFoundException(Long id) {
        super(String.format("🚪 Oops! Door #%d seems to be playing hide and seek. We couldn't find it in our records!", id));
    }

    public FurnitureDoorNotFoundException(String message) {
        super(message);
    }
}
