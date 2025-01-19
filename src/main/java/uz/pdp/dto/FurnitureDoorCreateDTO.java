package uz.pdp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.enums.FurnitureType;

/**
 * DTO for creating door accessories/furniture.
 * Because every door deserves some bling! 💎
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FurnitureDoorCreateDTO {

    @Schema(
        description = "Name of the door accessory",
        example = "Premium Door Handle",
        minLength = 2,
        maxLength = 100
    )
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Schema(
        description = "Material of the accessory (e.g., 'Stainless Steel', 'Brass')",
        example = "Stainless Steel",
        minLength = 2,
        maxLength = 50
    )
    @NotBlank(message = "Material is required")
    @Size(min = 2, max = 50, message = "Material description must be between 2 and 50 characters")
    private String material;

    @Schema(
        description = "Detailed description of the accessory",
        example = "Modern minimalist door handle with brushed finish",
        maxLength = 500
    )
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Schema(
        description = "Price in USD",
        example = "29.99",
        minimum = "0.01"
    )
    @Positive(message = "Price must be greater than zero")
    @NotNull(message = "Price is required")
    private Double price;

    @Schema(
        description = "Dimensions in millimeters (format: LxWxH)",
        example = "150x45x20",
        pattern = "^\\d{1,4}x\\d{1,4}x\\d{1,4}$"
    )
    @NotBlank(message = "Dimensions are required")
    @Pattern(
        regexp = "^\\d{1,4}x\\d{1,4}x\\d{1,4}$",
        message = "Dimensions must be in format LxWxH (e.g., '150x45x20'). Each dimension must be between 1-9999mm"
    )
    private String dimensions;

    @Schema(
        description = "Available quantity in stock",
        example = "100",
        minimum = "0"
    )
    @PositiveOrZero(message = "Stock quantity cannot be negative")
    @NotNull(message = "Stock quantity is required")
    private Integer stockQuantity;

    @Schema(
        description = "Type of door furniture (LOCK, HANDLE, etc.)",
        example = "HANDLE"
    )
    @NotNull(message = "Furniture type is required")
    private FurnitureType furnitureType;
}
