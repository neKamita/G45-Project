package uz.pdp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import uz.pdp.entity.CustomEnumValue;

/**
 * DTO for creating custom enum values.
 * 🎨 Like a paint swatch, but for your door options!
 * 
 * Only includes the fields that users should specify.
 * Other fields like createdBy, createdAt, and active are handled automatically.
 */
@Data
public class CustomEnumValueDto {
    @NotBlank(message = "Enum type is required")
    private String enumType;

    @NotBlank(message = "Display name is required")
    private String displayName;

    /**
     * Converts this DTO to an entity.
     * 🎭 The transformation station - where DTOs become entities!
     * 
     * Note: createdBy, createdAt, and active are set by the service layer.
     * 
     * @return A new CustomEnumValue entity with basic fields set
     */
    public CustomEnumValue toEntity() {
        CustomEnumValue entity = new CustomEnumValue();
        entity.setEnumType(this.enumType);
        entity.setDisplayName(this.displayName);
        entity.setName(this.displayName.toUpperCase()
                .replaceAll("[^A-Z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", ""));
        entity.setActive(true);
        return entity;
    }
}
