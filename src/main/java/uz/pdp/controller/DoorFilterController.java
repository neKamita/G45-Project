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
import uz.pdp.payload.EntityResponse;
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

    /**
     * Filters doors based on multiple criteria with smart fallback to partial matches.
     * 
     * @param locations Comma-separated list of door locations
     * @param frameTypes Comma-separated list of frame types
     * @param hardware Comma-separated list of hardware types
     * @param color Desired door color
     * @param size Door size in format: widthxheight (e.g., 200x2000)
     * @return EntityResponse containing matching doors or appropriate message
     * 
     * 🚪 Where doors meet their perfect match! Or at least their close friends! ✨
     */
    @Operation(summary = "Filter doors based on multiple criteria. Use comma-separated values for multiple options.")
    @GetMapping("/filter/{locations}/{frameTypes}/{hardware}/{color}/{size}")
    public ResponseEntity<EntityResponse<List<Door>>> filterDoors(
            @PathVariable String locations,
            @PathVariable String frameTypes,
            @PathVariable String hardware,
            @PathVariable String color,
            @PathVariable String size) {
        
        Set<DoorLocation> locationSet = new HashSet<>();
        Set<FrameType> frameTypeSet = new HashSet<>();
        Set<HardwareType> hardwareSet = new HashSet<>();

        // Parse locations
        for (String location : locations.split(",")) {
            try {
                locationSet.add(DoorLocation.valueOf(location.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        // Parse frame types
        for (String frameType : frameTypes.split(",")) {
            try {
                frameTypeSet.add(FrameType.valueOf(frameType.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        // Parse hardware types
        for (String h : hardware.split(",")) {
            try {
                hardwareSet.add(HardwareType.valueOf(h.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        // Convert size format from "200x2000" to "SIZE_200x2000"
        Size sizeEnum = null;
        if (size != null && !size.equalsIgnoreCase("none")) {
            try {
                sizeEnum = Size.valueOf("SIZE_" + size.toUpperCase());
            } catch (IllegalArgumentException e) {
                // If not a standard size, check if it's a custom size
                if (size.equalsIgnoreCase("custom")) {
                    sizeEnum = Size.CUSTOM;
                }
            }
        }

        // Try exact matches first
        List<Door> exactMatches = doorFilterService.filterDoors(DoorFilterDto.builder()
                .locations(locationSet)
                .frameTypes(frameTypeSet)
                .hardware(hardwareSet)
                .color(color != null && !color.equalsIgnoreCase("none") ? Color.valueOf(color.toUpperCase()) : null)
                .size(sizeEnum)
                .build());

        if (!exactMatches.isEmpty()) {
            return ResponseEntity.ok(EntityResponse.success(
                "Found your perfect doors! All criteria matched! 🎯✨",
                exactMatches
            ));
        }

        // If no exact matches, try partial matches (at least 2 criteria)
        List<Door> partialMatches = doorFilterService.findPartialMatches(
                locationSet, frameTypeSet, hardwareSet, color, size, 2);

        if (!partialMatches.isEmpty()) {
            return ResponseEntity.ok(EntityResponse.success(
                "🚪 We found some doors that partially match your criteria! " +
                "They might not be exactly what you're looking for, but they're close! ✨",
                partialMatches
            ));
        }

        // If still no matches, return the "no results" message
        return ResponseEntity.ok(EntityResponse.success(
            "🚪 Oops! We couldn't find any doors matching even 2 of your criteria. " +
            "Try adjusting your filters for more options - we've got lots of amazing doors to show you! ✨"
        ));
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
