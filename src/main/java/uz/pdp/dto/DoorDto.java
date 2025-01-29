package uz.pdp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uz.pdp.entity.Door;
import uz.pdp.enums.*;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * DTO for Door entity with all customization options.
 * 🚪 Every door tells a story, and this class tells it in JSON! 
 * 
 * Features:
 * - Basic info (name, description, price)
 * - Specifications (size, color, material)
 * - Hardware options (frame type, hardware type)
 * - Location details (interior, exterior, etc.)
 * - Custom measurements and colors
 */
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

    @NotNull(message = "Material is required")
    private String material;

    @NotNull(message = "Manufacturer is required")
    private String manufacturer;

    @NotNull(message = "Frame type is required")
    private FrameType frameType; // The type of frame (STANDARD, HIDDEN, etc.)

    @NotNull(message = "Hardware type is required")
    private HardwareType hardware; // The type of hardware (PIVOT, SLIDING, etc.)

    @NotNull(message = "Door location is required")
    private DoorLocation doorLocation; // Where the door is installed (INTERIOR, EXTERIOR, etc.)
    
    private Integer warrantyYears;
    private Double customWidth;
    private Double customHeight;
    private Boolean isCustomColor;

    private Double finalPrice; // Final price after calculations
    
    // Category fields - categoryId for input, categoryName for output
    private Long categoryId;      // Used when creating/updating doors

    private String categoryName;  // Used when returning door details
    
    private List<String> images; // Door images
    private String status; // Door status (AVAILABLE, etc)

    /**
     * Safely converts a Door entity to DTO, excluding sensitive seller data.
     * Like a bouncer at a club, but for door data! 🚪🔒
     * 
     * @param door The door entity to convert
     * @return A shiny new DoorDto with all the door's public details
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
        dto.setFrameType(door.getFrameType());
        dto.setHardware(door.getHardware());
        dto.setDoorLocation(door.getDoorLocation());
        dto.setWarrantyYears(door.getWarrantyYears());
        dto.setCustomWidth(door.getCustomWidth());
        dto.setCustomHeight(door.getCustomHeight());
        dto.setIsCustomColor(door.getIsCustomColor());
        dto.setStatus(door.getStatus().toString());
        dto.setImages(door.getImages());
        
        // Set just the category name if category is present
        if (door.getCategory() != null) {
            dto.setCategoryName(door.getCategory().getName());
            // Don't set categoryId in fromEntity to keep it out of responses
        }
        
        return dto;
    }
}