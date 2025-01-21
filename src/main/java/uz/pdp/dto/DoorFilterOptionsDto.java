package uz.pdp.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * DTO containing all available door filter options.
 * 
 * Like a door catalog that shows all possible choices! 📖🚪
 * This is your one-stop shop for all door customization options.
 */
@Data
@Builder
public class DoorFilterOptionsDto {
    private List<String> locations;     // Available door locations
    private List<String> frameTypes;    // Available frame types
    private List<String> hardware;      // Available hardware options
    private List<String> colors;        // Available colors
    private List<String> sizes;         // Available sizes
}
