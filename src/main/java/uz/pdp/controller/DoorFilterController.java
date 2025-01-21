package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.dto.DoorFilterDto;
import uz.pdp.dto.DoorFilterOptionsDto;
import uz.pdp.entity.Door;
import uz.pdp.enums.*;
import uz.pdp.service.DoorFilterService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller for door filtering operations.
 * 
 * Knock knock! 🚪 Who's there? The perfect door for you! 
 * This controller helps users find their ideal door through our filtering system.
 */
@RestController
@RequestMapping("/api/v1/doors")
@RequiredArgsConstructor
@Tag(name = "Door Filter", description = "API endpoints for filtering doors based on user preferences")
public class DoorFilterController {
    private final DoorFilterService doorFilterService;

    @Operation(summary = "Filter doors based on multiple criteria. Use comma-separated values for multiple options.")
    @GetMapping("/filter/{locations}/{frameTypes}/{hardware}/{color}/{size}")
    public ResponseEntity<List<Door>> filterDoors(
            @PathVariable(required = false) String locations,
            @PathVariable(required = false) String frameTypes,
            @PathVariable(required = false) String hardware,
            @PathVariable(required = false) String color,
            @PathVariable(required = false) String size) {
        
        // Parse multiple locations from display names
        Set<DoorLocation> locationSet = locations != null && !locations.equals("none") ?
            Arrays.stream(locations.split(","))
                .map(loc -> Arrays.stream(DoorLocation.values())
                    .filter(l -> l.getDisplayName().equalsIgnoreCase(loc))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid location: " + loc)))
                .collect(Collectors.toSet()) : new HashSet<>();

        // Parse multiple frame types from display names
        Set<FrameType> frameTypeSet = frameTypes != null && !frameTypes.equals("none") ?
            Arrays.stream(frameTypes.split(","))
                .map(frame -> Arrays.stream(FrameType.values())
                    .filter(f -> f.getDisplayName().equalsIgnoreCase(frame))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid frame type: " + frame)))
                .collect(Collectors.toSet()) : new HashSet<>();

        // Parse multiple hardware options from display names
        Set<HardwareType> hardwareSet = hardware != null && !hardware.equals("none") ?
            Arrays.stream(hardware.split(","))
                .map(hw -> Arrays.stream(HardwareType.values())
                    .filter(h -> h.getDisplayName().equalsIgnoreCase(hw))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid hardware: " + hw)))
                .collect(Collectors.toSet()) : new HashSet<>();

        // Parse color (single selection)
        Color colorEnum = color != null && !color.equals("none") ? Color.valueOf(color) : null;

        // Parse size (single selection)
        Size sizeEnum = null;
        if (size != null && !size.equals("none")) {
            if (size.equalsIgnoreCase("non-standard")) {
                sizeEnum = Size.CUSTOM;
            } else {
                String[] dimensions = size.split("x");
                if (dimensions.length == 2) {
                    int width = Integer.parseInt(dimensions[0]);
                    int height = Integer.parseInt(dimensions[1]);
                    sizeEnum = Arrays.stream(Size.values())
                        .filter(s -> s != Size.CUSTOM && s.getWidth() == width && s.getHeight() == height)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Invalid size: " + size));
                }
            }
        }
        
        return ResponseEntity.ok(doorFilterService.filterDoors(DoorFilterDto.builder()
                .locations(locationSet)
                .frameTypes(frameTypeSet)
                .hardware(hardwareSet)
                .color(colorEnum)
                .size(sizeEnum)
                .build()));
    }

    @Operation(summary = "Get all available filter options")
    @GetMapping("/options")
    public ResponseEntity<DoorFilterOptionsDto> getFilterOptions() {
        return ResponseEntity.ok(DoorFilterOptionsDto.builder()
            .locations(Arrays.stream(DoorLocation.values())
                .map(DoorLocation::getDisplayName)
                .collect(Collectors.toList()))
            .frameTypes(Arrays.stream(FrameType.values())
                .map(FrameType::getDisplayName)
                .collect(Collectors.toList()))
            .hardware(Arrays.stream(HardwareType.values())
                .map(HardwareType::getDisplayName)
                .collect(Collectors.toList()))
            .colors(Arrays.stream(Color.values())
                .map(Enum::name)
                .collect(Collectors.toList()))
            .sizes(Arrays.stream(Size.values())
                .map(size -> size == Size.CUSTOM ? "non-standard" : 
                    size.getWidth() + "x" + size.getHeight())
                .collect(Collectors.toList()))
            .build());
    }
}
