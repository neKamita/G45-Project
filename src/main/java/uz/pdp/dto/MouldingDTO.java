package uz.pdp.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for Moulding operations.
 * Because every moulding needs its own identity card! 🪪
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MouldingDTO {
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    private String title;

    @NotBlank(message = "Size is required")
    @Pattern(regexp = "^\\d+(\\.\\d+)?x\\d+(\\.\\d+)?$", 
            message = "Size must be in format: widthxheight (e.g., 75x38 or 75.5x38.5)")
    private String size;

    @NotBlank(message = "Article code is required")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Article must contain only uppercase letters, numbers and hyphens")
    private String article;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Price cannot exceed 999,999.99")
    private Double price;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 999999, message = "Quantity cannot exceed 999,999")
    private Integer quantity;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Size(max = 10, message = "Cannot upload more than 10 images")
    private List<String> imagesUrl;

    // Optional seller ID - will be set automatically for SELLER role, 
    // but ADMIN can specify a different seller
    private Long sellerId;

    /**
     * Validates size format.
     * @return true if size is in valid format (e.g., "10x20" or "10.5x20.5")
     */
    public boolean isValidSizeFormat() {
        if (size == null) return false;
        return size.matches("^\\d+(\\.\\d+)?x\\d+(\\.\\d+)?$");
    }
}
