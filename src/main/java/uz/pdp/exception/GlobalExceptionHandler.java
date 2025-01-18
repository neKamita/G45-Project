package uz.pdp.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uz.pdp.payload.ErrorResponse;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
        Map<String, String> errors = ex.getErrors();
        String message = errors != null && !errors.isEmpty() 
            ? "Validation failed: " + errors.values().iterator().next()  // Get the first error message
            : ex.getMessage();
            
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            message,
            request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.UNAUTHORIZED.value(),
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error occurred", ex);
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Unable to process your request. Please try again later.",
            request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
