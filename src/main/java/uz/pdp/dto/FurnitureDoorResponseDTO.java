package uz.pdp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.enums.FurnitureType;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for returning furniture door data in API responses.
 * Think of it as a door's resume - showing off its best features! 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FurnitureDoorResponseDTO {
    @Schema(description = "Unique identifier", example = "1")
    private Long id;

    @Schema(description = "Name of the door", example = "Premium Door")
    private String name;

    @Schema(description = "Material of the door", example = "Wood")
    private String material;

    @Schema(description = "Description of the door", example = "Modern minimalist door with wooden finish")
    private String description;

    @Schema(description = "Price in USD", example = "99.99")
    private Double price;

    @Schema(description = "Dimensions in format LxWxH", example = "200x80x20")
    private String dimensions;

    @Schema(description = "Available quantity in stock", example = "50")
    private Integer stockQuantity;

    @Schema(description = "Type of door furniture", example = "DOOR")
    private FurnitureType furnitureType;

    @Schema(description = "URLs of the door images", example = "[\"https://your-bucket.s3.amazonaws.com/doors/image1.jpg\", \"https://your-bucket.s3.amazonaws.com/doors/image2.jpg\"]")
    private List<String> imageUrls = new ArrayList<>();

}
