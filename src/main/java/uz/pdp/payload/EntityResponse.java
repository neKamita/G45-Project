package uz.pdp.payload;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import uz.pdp.entity.Moulding;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityResponse<T> {
    private boolean success;
    private String message;
    private T data;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
    private Instant timestamp = Instant.now();

    public EntityResponse(String messsage, boolean b, Object o) {
        this.message = messsage;
        this.success = b;
        this.data = (T) o;
    }


    public static <T> EntityResponse<T> success(String message) {
        return success(message, null);
    }

    public static <T> EntityResponse<T> success(String message, T data) {
        EntityResponse<T> response = new EntityResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(Instant.now());
        return response;
    }

    public static <T> EntityResponse<T> error(String message) {
        return error(message, null);
    }

    public static <T> EntityResponse<T> error(String message, T data) {
        EntityResponse<T> response = new EntityResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(Instant.now());
        return response;
    }

    public static Object success(@NotNull Moulding moulding) {
        return new EntityResponse<>("Moulding created successfully", true, moulding);
    }
}