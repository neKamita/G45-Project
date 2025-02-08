package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.dto.DoorFilterDto;
import uz.pdp.dto.DoorFilterOptionsDto;
import uz.pdp.entity.Door;
import uz.pdp.enums.*;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.DoorFilterService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(DoorFilterController.class);

    /**
     * 🚪 Where doors meet their perfect match! Or at least their close friends! ✨
     *
     * @param locations Locations to filter by (comma-separated)
     * @param frameTypes Frame types to filter by (comma-separated)
     * @param hardware Hardware types to filter by (comma-separated)
     * @param color Color to filter by
     * @param size Size to filter by (format: widthxheight)
     * @return Filtered list of doors with a fun message
     */
    @Operation(summary = "Filter doors based on multiple criteria. Use comma-separated values for multiple options.")
    @GetMapping("/filter")
    public ResponseEntity<EntityResponse<List<Door>>> filterDoors(
            @Parameter(description = "Locations (comma-separated)", required = false) @RequestParam(required = false) String locations,
            @Parameter(description = "Frame Types (comma-separated)", required = false) @RequestParam(required = false) String frameTypes,
            @Parameter(description = "Hardware (comma-separated)", required = false) @RequestParam(required = false) String hardware,
            @Parameter(description = "Color", required = false) @RequestParam(required = false) String color,
            @Parameter(description = "Size (widthxheight, e.g., 200x2000)", required = false) @RequestParam(required = false) String size) {
        
        try {
            List<Door> filteredDoors = doorFilterService.filterDoors(locations, frameTypes, hardware, color, size);
            String message = filteredDoors.isEmpty() 
                ? "No doors matched your criteria. Don't worry, we'll keep knocking on opportunities! 🚪" 
                : String.format("Found %d doors that match your style! Ready to make an entrance? 🎉", filteredDoors.size());
            
            return ResponseEntity.ok(EntityResponse.success(message, filteredDoors));
        } catch (Exception e) {
            log.error("Error filtering doors: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EntityResponse.error("Oops! Our door matcher is having a moment. We're working on it! 🔧"));
        }
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
