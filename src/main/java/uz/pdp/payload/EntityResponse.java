package uz.pdp.payload;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import uz.pdp.entity.Moulding;

import java.io.Serializable;
import java.time.Instant;

/**
 * A wrapper class for API responses.
 * Because every response needs a nice suit to wear! 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class EntityResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private boolean success;

    @JsonProperty
    private String message;

    @JsonProperty
    private T data;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
    @JsonProperty
    private Instant timestamp = Instant.now();

    public EntityResponse(String message, boolean success, T data) {
        this.message = message;
        this.success = success;
        this.data = data;
        this.timestamp = Instant.now();
    }

    public static <T> EntityResponse<T> success(String message) {
        return success(message, null);
    }

    public static <T> EntityResponse<T> success(String message, T data) {
        return new EntityResponse<>(message, true, data);
    }

    public static <T> EntityResponse<T> error(String message) {
        return error(message, null);
    }

    public static <T> EntityResponse<T> error(String message, T data) {
        return new EntityResponse<>(message, false, data);
    }

    public static EntityResponse<Moulding> success(@NotNull Moulding moulding) {
        return new EntityResponse<>("Moulding created successfully", true, moulding);
    }
}