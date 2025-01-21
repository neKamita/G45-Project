package uz.pdp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.enums.*;

import java.util.Set;

/**
 * DTO for filtering doors based on user preferences.
 * 
 * Like a doorman who knows exactly what you're looking for! 🚪🔍
 * This class helps match customers with their dream doors.
 * Multi-select available for location, frame type, and hardware! ✨
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoorFilterDto {
    private Set<DoorLocation> locations;    
    private Set<FrameType> frameTypes;     
    private Set<HardwareType> hardware;    
    private Color color;                    
    private Size size;                     
}
