package uz.pdp.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntityResponse<T> {
    private int statusCode;
    private String message;
    private T data;
    private long timestamp;

    public EntityResponse(HttpStatus status, String message, T data) {
        this.statusCode = status.value();
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> EntityResponse<T> success(T data) {
        return new EntityResponse<>(HttpStatus.OK, "Success", data);
    }

    public static <T> EntityResponse<T> success(String message, T data) {
        return new EntityResponse<>(HttpStatus.OK, message, data);
    }

    public static <T> EntityResponse<T> created(T data) {
        return new EntityResponse<>(HttpStatus.CREATED, "Created successfully", data);
    }

    public static EntityResponse<Void> deleted() {
        return new EntityResponse<>(HttpStatus.NO_CONTENT, "Deleted successfully", null);
    }
} 