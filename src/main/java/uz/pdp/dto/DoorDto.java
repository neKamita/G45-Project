package uz.pdp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uz.pdp.entity.Door;
import uz.pdp.enums.Color;
import uz.pdp.enums.Size;

import java.util.List;

@Data
@Getter
@Setter
public class DoorDto {
    private Long id; // Adding ID for client reference

    @NotNull(message = "Name is required")
    private String name;

    @NotNull(message = "Description is required")
    private String description;

    @Positive(message = "Price must be greater than 0")
    private Double price;

    @NotNull(message = "Size is required")
    private Size size;

    @NotNull(message = "Color is required")
    private Color color;

    private String material;
    private String manufacturer;
    private Integer warrantyYears;
    private Double customWidth;
    private Double customHeight;
    private Boolean isCustomColor;

    private Double finalPrice; // Final price after calculations
    private List<String> images; // Door images
    private String status; // Door status (AVAILABLE, etc)

    /**
     * Safely converts a Door entity to DTO, excluding sensitive seller data
     * Like a bouncer at a club, but for door data! 🚪🔒
     */
    public static DoorDto fromEntity(Door door) {
        DoorDto dto = new DoorDto();
        dto.setId(door.getId());
        dto.setName(door.getName());
        dto.setDescription(door.getDescription());
        dto.setPrice(door.getPrice());
        dto.setFinalPrice(door.getFinalPrice());
        dto.setSize(door.getSize());
        dto.setColor(door.getColor());
        dto.setMaterial(door.getMaterial());
        dto.setManufacturer(door.getManufacturer());
        dto.setWarrantyYears(door.getWarrantyYears());
        dto.setCustomWidth(door.getCustomWidth());
        dto.setCustomHeight(door.getCustomHeight());
        dto.setIsCustomColor(door.getIsCustomColor());
        dto.setImages(door.getImages());
        dto.setStatus(door.getStatus().toString());
        return dto;
    }
}