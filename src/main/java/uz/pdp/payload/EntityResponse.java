package uz.pdp.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntityResponse<T> {
    private String message;
    private T data;
    private boolean success;
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> EntityResponse<T> success(T data) {
        return new EntityResponse<T>("Success", data, true, LocalDateTime.now());
    }

    public static <T> EntityResponse<T> success(String message, T data) {
        return new EntityResponse<>(message, data, true, LocalDateTime.now()); 
    }

    public static <T> EntityResponse<T> created(T data) {
        return new EntityResponse<>("Created successfully", data, true, LocalDateTime.now());
    }

    public static <T> EntityResponse<T> created(String message, T data) {
        return new EntityResponse<>(message, data, true, LocalDateTime.now());
    }

    public static EntityResponse<Void> deleted() {
        return new EntityResponse<>("Deleted successfully", null, true, LocalDateTime.now());
    }

    public static Object error(String message2) {
        
        return new EntityResponse<>(message2, null, false, LocalDateTime.now());

    }
}