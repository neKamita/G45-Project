package uz.pdp.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import uz.pdp.enums.Socials;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Street is required")
    private String street;
    
    @NotBlank(message = "City is required")
    private String city;
    
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    
    private String workingHours;
    private String email;
    private boolean isDefault;
    
    @NotNull(message = "Location information is required")
    private LocationDTO location;
    
    private Map<Socials, String> socialLinks;
    
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