package uz.pdp.exception;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import uz.pdp.payload.EntityResponse;
import uz.pdp.enums.Color;
import uz.pdp.enums.Size;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Central exception handling and custom exceptions for our application.
 * One file to rule them all! 
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Base exception class for our application.
     * The grandfather of all our custom exceptions! 
     */
    @Getter
    public static abstract class BaseException extends RuntimeException {
        private final HttpStatus status;

        protected BaseException(String message, HttpStatus status) {
            super(message);
            this.status = status;
        }
    }

    /**
     * Exception for when a furniture door cannot be found.
     * Because sometimes doors just don't want to be found! 
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class FurnitureDoorNotFoundException extends BaseException {
        public FurnitureDoorNotFoundException(Long id) {
            super(String.format(" Oops! Door #%d seems to be playing hide and seek. We couldn't find it in our records!", id),
                  HttpStatus.NOT_FOUND);
        }

    }

    /**
     * Exception for bad requests.
     * When users try to push when they should pull! 
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class BadRequestException extends BaseException {
        public BadRequestException(String message) {
            super(" " + message, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Exception for unauthorized access.
     * No ticket, no entry! 
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class UnauthorizedException extends BaseException {
        public UnauthorizedException(String message) {
            super(" " + message, HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Exception for when any resource is not found.
     * The classic 404 - but make it fancy! 
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class ResourceNotFoundException extends BaseException {
        public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
            super(String.format(" %s not found with %s: '%s'", resourceName, fieldName, fieldValue),
                  HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Handles all custom base exceptions.
     * One handler to catch them all! 
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<EntityResponse<Void>> handleBaseException(BaseException ex) {
        log.error("Base exception occurred: {}", ex.getMessage());
        return ResponseEntity
                .status(ex.getStatus())
                .body(EntityResponse.error(ex.getMessage()));
    }

    /**
     * Handles validation exceptions.
     * Making sure all our i's are dotted and t's are crossed! 
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<EntityResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        
        log.error("Validation exception occurred: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(EntityResponse.error(" " + message));
    }

    /**
     * Handles HttpMessageNotReadableException.
     * Making sure all our JSON is properly formatted! 
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<EntityResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        String message;
        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) ex.getCause();
            String fieldName = ife.getPath().get(0).getFieldName();
            
            if (fieldName.equals("color")) {
                message = String.format("Invalid color value");
            } else if (fieldName.equals("size")) {
                message = String.format("Invalid size value");
            } else {
                message = String.format("Invalid value for field '%s'", fieldName);
            }
        } else {
            message = "Invalid request format. Please check your input.";
        }
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(EntityResponse.error(message));
    }

    /**
     * Handles JSON serialization errors with grace and humor.
     * Because even doors have their moments of confusion! 🚪
     *
     * @param ex The exception that occurred
     * @return A user-friendly error response
     */
    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<EntityResponse<?>> handleHttpMessageNotWritableException(HttpMessageNotWritableException ex) {
        logger.error("Failed to serialize response: ", ex);
        
        EntityResponse<?> response = EntityResponse.builder()
                .success(false)
                .message("Oops! Our doors are having an identity crisis! Don't worry, our door whisperers are on it! 🚪✨")
                .timestamp(Instant.from(LocalDateTime.now()))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Handles media type not supported exceptions.
     * When someone tries to sneak in a file type we don't like! 🕵️‍♂️
     */
    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<EntityResponse<Void>> handleHttpMediaTypeNotSupportedException(
            org.springframework.web.HttpMediaTypeNotSupportedException ex) {
        logger.error("Media type not supported: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(EntityResponse.error(
                    "Oops! That's not the file type we were expecting! We only accept multipart/form-data with proper image files! 📸"));
    }

    /**
     * Handles all unexpected exceptions.
     * Because sometimes even exception handlers need a backup plan! 
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<EntityResponse<Void>> handleUnexpectedException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(EntityResponse.error(" Oops! Something unexpected happened. Our door experts are on it!"));
    }

    /**
     * Handles AWS S3 service exceptions.
     * When the cloud storage decides to take a coffee break! ☕
     */
    @ExceptionHandler(AmazonServiceException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public EntityResponse<Object> handleAmazonServiceException(AmazonServiceException ex) {
        log.error("AWS S3 service error: {}", ex.getMessage());
        return EntityResponse.error("Image storage service is temporarily unavailable. Please try again later! 🔄");
    }

    /**
     * Handles AWS SDK client exceptions.
     * When our connection to the cloud gets cloudy! ⛈️
     */
    @ExceptionHandler(SdkClientException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public EntityResponse<Object> handleSdkClientException(SdkClientException ex) {
        log.error("AWS SDK client error: {}", ex.getMessage());
        return EntityResponse.error("Unable to connect to image storage service. Please try again later! 🌐");
    }

    /**
     * Handles file size limit exceeded exceptions.
     * When someone tries to upload the entire photo album! 📚
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public EntityResponse<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.error("File size limit exceeded: {}", ex.getMessage());
        return EntityResponse.error("Image size exceeds the maximum limit of 5MB! Please compress the image and try again 📸");
    }

    /**
     * Handles general IO exceptions during file operations.
     * When bits and bytes decide to play hide and seek! 🙈
     */
    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public EntityResponse<Object> handleIOException(IOException ex) {
        log.error("IO error during file operation: {}", ex.getMessage());
        return EntityResponse.error("Failed to process the image. Please try again with a different image! 🖼️");
    }

    /**
     * Handles illegal argument exceptions for invalid file types.
     * When someone tries to upload their grocery list as an image! 📝
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public EntityResponse<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage());
        return EntityResponse.error(ex.getMessage() + " 🚫");
    }
}
