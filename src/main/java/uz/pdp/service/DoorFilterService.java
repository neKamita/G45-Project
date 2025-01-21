package uz.pdp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.pdp.dto.DoorFilterDto;
import uz.pdp.entity.Door;
import uz.pdp.enums.DoorLocation;
import uz.pdp.enums.FrameType;
import uz.pdp.enums.HardwareType;
import uz.pdp.repository.DoorFilterRepository;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
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
}
