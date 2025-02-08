package uz.pdp.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.pdp.dto.DoorFilterDto;
import uz.pdp.entity.Door;
import uz.pdp.enums.DoorLocation;
import uz.pdp.enums.FrameType;
import uz.pdp.enums.HardwareType;
import uz.pdp.repository.DoorFilterRepository;
import uz.pdp.repository.DoorRepository;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for filtering doors based on user preferences.
 * 
 * Like a door whisperer, this service knows how to find the perfect door! 🚪💫
 * It takes user preferences and returns a collection of matching doors.
 * Now with multi-select support for location, frame type, and hardware! 🎯
 */
@Service
@RequiredArgsConstructor
public class DoorFilterService {
    private final DoorFilterRepository doorFilterRepository;
    private final DoorRepository doorRepository;
    private static final Logger log = LoggerFactory.getLogger(DoorFilterService.class);

    public List<Door> filterDoors(DoorFilterDto filterDto) {
        // I don`t know how this works but this works just do not touch 
        // If you know how it works please tell me
        Specification<Door> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Add location filter (multiple options)
            if (filterDto.getLocations() != null && !filterDto.getLocations().isEmpty()) {
                predicates.add(root.get("doorLocation").in(filterDto.getLocations()));
            }

            // Add frame type filter (multiple options)
            if (filterDto.getFrameTypes() != null && !filterDto.getFrameTypes().isEmpty()) {
                predicates.add(root.get("frameType").in(filterDto.getFrameTypes()));
            }

            // Add hardware filter (multiple options)
            if (filterDto.getHardware() != null && !filterDto.getHardware().isEmpty()) {
                predicates.add(root.get("hardware").in(filterDto.getHardware()));
            }

            // Add color filter (single option)
            if (filterDto.getColor() != null) {
                predicates.add(cb.equal(root.get("color"), filterDto.getColor()));
            }

            // Add size filter (single option)
            if (filterDto.getSize() != null) {
                predicates.add(cb.equal(root.get("size"), filterDto.getSize()));
            }

            // Only show active doors
            predicates.add(cb.equal(root.get("active"), true));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return doorFilterRepository.findAll(spec);
    }

    /**
     * Finds doors that match at least the specified number of criteria.
     * When exact matches aren't found, this helps users discover similar doors! 🚪
     * 
     * @param locations Set of door locations to match
     * @param frameTypes Set of frame types to match
     * @param hardwareTypes Set of hardware types to match
     * @param color Color to match
     * @param size Size to match
     * @param minimumMatchingCriteria Minimum number of criteria that must match
     * @return List of doors matching at least the minimum criteria
     * 
     * Think of it as a door matchmaker - finding your door soulmate! 💕
     */
    public List<Door> findPartialMatches(
            Set<DoorLocation> locations,
            Set<FrameType> frameTypes,
            Set<HardwareType> hardwareTypes,
            String color,
            String size,
            int minimumMatchingCriteria) {
        
        return doorFilterRepository.findAll().stream()
            .filter(door -> {
                int matchCount = 0;
                
                if (locations.contains(door.getDoorLocation())) matchCount++;
                if (frameTypes.contains(door.getFrameType())) matchCount++;
                if (hardwareTypes.contains(door.getHardware())) matchCount++;
                if (color != null && color.equalsIgnoreCase(door.getColor().name())) matchCount++;
                if (size != null && size.equalsIgnoreCase(door.getSize().name())) matchCount++;
                
                return matchCount >= minimumMatchingCriteria;
            })
            .collect(Collectors.toList());
    }

    /**
     * 🚪 The magical door filter that helps find your perfect match! 
     * Think of it as a dating app, but for doors. Swipe right for the perfect door! 
     *
     * @param locations Door locations to filter by (comma-separated)
     * @param frameTypes Frame types to filter by (comma-separated)
     * @param hardware Hardware types to filter by (comma-separated)
     * @param color Color to filter by
     * @param size Size to filter by (format: widthxheight)
     * @return List of doors matching the criteria
     */
    public List<Door> filterDoors(String locations, String frameTypes, String hardware, String color, String size) {
        log.debug("Filtering doors with params - locations: {}, frameTypes: {}, hardware: {}, color: {}, size: {}", 
                  locations, frameTypes, hardware, color, size);

        List<Door> allDoors = doorRepository.findAll();
        log.debug("Found {} doors before filtering", allDoors.size());
        
        List<Door> filteredDoors = allDoors.stream()
                .filter(door -> {
                    boolean locationMatch = isMatchingLocation(door, locations);
                    log.debug("Door {} location match: {}", door.getId(), locationMatch);
                    return locationMatch;
                })
                .filter(door -> {
                    boolean hardwareMatch = isMatchingHardware(door, hardware);
                    log.debug("Door {} hardware match: {}", door.getId(), hardwareMatch);
                    return hardwareMatch;
                })
                .filter(door -> {
                    boolean colorMatch = isMatchingColor(door, color);
                    log.debug("Door {} color match: {}", door.getId(), colorMatch);
                    return colorMatch;
                })
                .filter(door -> {
                    boolean sizeMatch = isMatchingSize(door, size);
                    log.debug("Door {} size match: {}", door.getId(), sizeMatch);
                    return sizeMatch;
                })
                .collect(Collectors.toList());

        log.debug("Found {} doors after filtering", filteredDoors.size());
        return filteredDoors;
    }

    private boolean isMatchingLocation(Door door, String locations) {
        if (locations == null || locations.isEmpty() || "none".equalsIgnoreCase(locations)) {
            return true;
        }
        if (door.getDoorLocation() == null) {
            log.debug("Door {} has null location", door.getId());
            return false;
        }
        
        // Convert input locations to enum values
        boolean matches = Arrays.stream(locations.split(","))
            .map(String::trim)
            .anyMatch(loc -> {
                // Try to find matching enum by display name
                return Arrays.stream(DoorLocation.values())
                    .anyMatch(enumLoc -> enumLoc.getDisplayName().equalsIgnoreCase(loc) && 
                                       enumLoc == door.getDoorLocation());
            });
        
        log.debug("Door {} location {} matches {}: {}", 
            door.getId(), 
            door.getDoorLocation().getDisplayName(), 
            locations, 
            matches);
            
        return matches;
    }

    private boolean isMatchingHardware(Door door, String hardware) {
        if (hardware == null || hardware.isEmpty() || "none".equalsIgnoreCase(hardware)) {
            return true;
        }
        if (door.getHardware() == null) {
            log.debug("Door {} has null hardware", door.getId());
            return false;
        }
        boolean matches = Arrays.stream(hardware.split(","))
                     .map(String::trim)
                     .anyMatch(hw -> door.getHardware().getDisplayName().equalsIgnoreCase(hw));
        log.debug("Door {} hardware {} matches {}: {}", door.getId(), door.getHardware().getDisplayName(), hardware, matches);
        return matches;
    }

    private boolean isMatchingColor(Door door, String color) {
        if (color == null || color.isEmpty() || "none".equalsIgnoreCase(color)) {
            return true;
        }
        if (door.getColor() == null) {
            log.debug("Door {} has null color", door.getId());
            return false;
        }
        boolean matches = door.getColor().getDisplayName().equalsIgnoreCase(color.trim());
        log.debug("Door {} color {} matches {}: {}", door.getId(), door.getColor().getDisplayName(), color, matches);
        return matches;
    }

    private boolean isMatchingSize(Door door, String size) {
        if (size == null || size.isEmpty() || "none".equalsIgnoreCase(size)) {
            return true;
        }
        if (door.getSize() == null) {
            log.debug("Door {} has null size", door.getId());
            return false;
        }
        
        try {
            // Try to match the size format (e.g., "200x2000")
            String[] dimensions = size.split("x");
            if (dimensions.length == 2) {
                int width = Integer.parseInt(dimensions[0]);
                int height = Integer.parseInt(dimensions[1]);
                
                boolean matches = door.getSize().getWidth() == width && 
                                door.getSize().getHeight() == height;
                log.debug("Door {} size {}x{} matches {}x{}: {}", 
                         door.getId(), door.getSize().getWidth(), door.getSize().getHeight(), 
                         width, height, matches);
                return matches;
            }
            
            // If not in WxH format, try as enum name
            boolean matches = door.getSize().name().equalsIgnoreCase(size.trim());
            log.debug("Door {} size {} matches enum {}: {}", door.getId(), door.getSize(), size, matches);
            return matches;
        } catch (Exception e) {
            log.debug("Error parsing size '{}' for door {}: {}", size, door.getId(), e.getMessage());
            return false;
        }
    }
}
