package uz.pdp.payload;

public record EntityResponse<T>(
    String message,
    T data,
    boolean success,
    String timestamp
) {
    // Static factory methods for success responses
    public static <T> EntityResponse<T> success(String message) {
        return new EntityResponse<>(message, null, true, java.time.LocalDateTime.now().toString());
    }

    public static <T> EntityResponse<T> success(String message, T data) {
        return new EntityResponse<>(message, data, true, java.time.LocalDateTime.now().toString());
    }

    // Static factory methods for error responses
    public static <T> EntityResponse<T> error(String message) {
        return new EntityResponse<>(message, null, false, java.time.LocalDateTime.now().toString());
    }

    public static <T> EntityResponse<T> error(String message, T data) {
        return new EntityResponse<>(message, data, false, java.time.LocalDateTime.now().toString());
    }

}