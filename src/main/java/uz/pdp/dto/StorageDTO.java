package uz.pdp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.enums.ItemType;

import java.util.Set;

/**
 * DTO for transferring storage information.
 * The digital blueprint of our door storage locations! 📦
 *
 * @version 1.0
 * @since 2025-02-10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StorageDTO {
    private Long id;

    @NotBlank(message = "Storage name is required")
    private String name;

    @NotNull(message = "Location information is required")
    private LocationDTO location;

    @NotNull(message = "Product types are required")
    private Set<ItemType> productTypes;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LocationDTO {
        @NotNull(message = "Latitude is required")
        private Double latitude;

        @NotNull(message = "Longitude is required")
        private Double longitude;

        private String markerTitle;
    }
}
