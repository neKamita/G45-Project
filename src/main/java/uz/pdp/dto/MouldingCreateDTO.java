package uz.pdp.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for creating new mouldings.
 * Like a birth certificate for baby mouldings! 👶🪚
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MouldingCreateDTO {

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    private String title;

    @NotBlank(message = "Size is required")
    @Pattern(regexp = "^\\d+(\\.\\d+)?x\\d+(\\.\\d+)?$", 
            message = "Size must be in format: widthxheight (e.g., 75x38 or 75.5x38.5)")
    private String size;

    @NotBlank(message = "Article code is required")
    @Pattern(regexp = "^[A-Z0-9-]+$", 
            message = "Article must contain only uppercase letters, numbers and hyphens")
    @Size(max = 50, message = "Article cannot be longer than 50 characters")
    private String article;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Price cannot exceed 999,999.99")
    @Digits(integer = 6, fraction = 2, message = "Price must have at most 6 digits before decimal and 2 after")
    private Double price;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 999999, message = "Quantity cannot exceed 999,999")
    private Integer quantity;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Size(max = 10, message = "Cannot upload more than 10 images")
    private List<String> imagesUrl;

    /**
     * Validates size dimensions.
     * Making sure our moulding isn't trying to be a skyscraper! 🏢
     * 
     * @return true if dimensions are within acceptable range (0-1000)
     */
    public boolean isValidDimensions() {
        try {
            String[] parts = size.split("x");
            double width = Double.parseDouble(parts[0]);
            double height = Double.parseDouble(parts[1]);
            return width > 0 && width <= 1000 && height > 0 && height <= 1000;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates article format.
     * Because even mouldings need proper identification! 🪪
     * 
     * @return true if article format is valid
     */
    public boolean isValidArticle() {
        return article != null && 
               article.matches("^[A-Z0-9-]+$") && 
               article.length() <= 50;
    }
}
