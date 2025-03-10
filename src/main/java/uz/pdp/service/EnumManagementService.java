package uz.pdp.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.entity.CustomEnumValue;
import uz.pdp.entity.User;
import uz.pdp.enums.*;
import uz.pdp.exception.UnauthorizedException;
import uz.pdp.repository.CustomEnumValueRepository;
import uz.pdp.repository.UserRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing custom enum values.
 * 🎨 Making your door options as unique as you are!
 * 
 * Handles:
 * - Adding new custom enum values
 * - Retrieving all enum values (built-in + custom)
 * - Deactivating custom enum values
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EnumManagementService {
    private static final Logger logger = LoggerFactory.getLogger(EnumManagementService.class);
    
    private final CustomEnumValueRepository customEnumValueRepository;
    private final UserRepository userRepository;

    /**
     * Adds a new custom enum value.
     * 🎨 Like adding a new color to your palette!
     * 
     * @param customEnumValue The enum value to add
     * @return The saved enum value
     * @throws UnauthorizedException if user is not authenticated
     * @throws IllegalArgumentException if enum type is invalid or value already exists
     */
    public CustomEnumValue addCustomEnumValue(CustomEnumValue customEnumValue) {
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("You must be logged in to add custom enum values! 🔐");
        }

        String name = authentication.getName();
        logger.debug("Looking up user by username: {}", name);
        
        User currentUser = userRepository.findByName(name)
                .orElseThrow(() -> new UnauthorizedException(
                    "Hmm... We can't find your user account. Are you sure you're logged in? 🤔 (name: " + name + ")"
                ));

        // Validate enum type
        validateEnumType(customEnumValue.getEnumType());

        // Convert display name to enum name format (uppercase, underscored)
        String enumName = convertToEnumName(customEnumValue.getDisplayName());
        
        // Check if already exists
        if (customEnumValueRepository.existsByNameAndEnumType(enumName, customEnumValue.getEnumType())) {
            throw new IllegalArgumentException(
                "This enum value already exists! Try something more unique. 🎯"
            );
        }

        // Set the enum name and creator
        customEnumValue.setName(enumName);
        customEnumValue.setCreatedBy(currentUser);
        customEnumValue.setActive(true);

        logger.debug("Creating new enum value: {} of type {} by user {}", 
                    enumName, customEnumValue.getEnumType(), name);
                    
        return customEnumValueRepository.save(customEnumValue);
    }

    public List<String> getAllEnumValues(String enumType) {
        validateEnumType(enumType);
        
        List<String> allValues = new ArrayList<>();
        
        // Add built-in enum values
        switch (enumType) {
            case "DoorMaterial":
                allValues.addAll(Arrays.stream(DoorMaterial.values())
                        .map(DoorMaterial::getDisplayName)
                        .collect(Collectors.toList()));
                break;
            case "DoorStyle":
                allValues.addAll(Arrays.stream(DoorStyle.values())
                        .map(DoorStyle::getDisplayName)
                        .collect(Collectors.toList()));
                break;
            case "DoorManufacturer":
                allValues.addAll(Arrays.stream(DoorManufacturer.values())
                        .map(DoorManufacturer::getDisplayName)
                        .collect(Collectors.toList()));
                break;
            case "HardwareType":
                allValues.addAll(Arrays.stream(HardwareType.values())
                        .map(HardwareType::getDisplayName)
                        .collect(Collectors.toList()));
                break;
            case "Color":
                allValues.addAll(Arrays.stream(Color.values())
                        .map(Enum::name)
                        .collect(Collectors.toList()));
                break;
            case "Size":
                allValues.addAll(Arrays.stream(Size.values())
                        .map(size -> String.format("%dx%d", size.getWidth(), size.getHeight()))
                        .collect(Collectors.toList()));
                break;
            default:
                throw new IllegalArgumentException(
                    "Invalid enum type! Available types: DoorMaterial, DoorStyle, DoorManufacturer, HardwareType, Color, Size"
                );
        }
        
        // Add custom enum values
        List<String> customValues = customEnumValueRepository.findByEnumTypeAndIsActiveTrue(enumType)
                .stream()
                .map(CustomEnumValue::getDisplayName)
                .collect(Collectors.toList());
        
        allValues.addAll(customValues);
        
        return allValues;
    }

    public void deactivateEnumValue(Long id) {
        CustomEnumValue enumValue = customEnumValueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Custom enum value not found"));
        
        enumValue.setActive(false);
        customEnumValueRepository.save(enumValue);
    }

    private void validateEnumType(String enumType) {
        if (!Arrays.asList("DoorMaterial", "DoorStyle", "DoorManufacturer", "HardwareType", "Color", "Size")
                .contains(enumType)) {
            throw new IllegalArgumentException(
                "Invalid enum type! Available types: DoorMaterial, DoorStyle, DoorManufacturer, HardwareType, Color, Size"
            );
        }
    }

    private String convertToEnumName(String displayName) {
        return displayName.toUpperCase()
                .replaceAll("[^A-Z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }

    /**
     * Get example values for each enum type.
     * 🎨 A showcase of our door customization options!
     * 
     * @return Map of enum types to their example values
     */
    public Map<String, List<String>> getEnumExamples() {
        Map<String, List<String>> examples = new HashMap<>();
        examples.put("DoorMaterial", List.of(
            "Solid Oak", "Mahogany", "Walnut", "Pine", 
            "Cherry Wood", "Bamboo", "Carbon Fiber", "Steel"
        ));
        examples.put("DoorStyle", List.of(
            "Modern", "Victorian", "Rustic", "Contemporary",
            "Art Deco", "Minimalist", "Craftsman", "Colonial"
        ));
        examples.put("DoorManufacturer", List.of(
            "DoorMaster Pro", "Portal Paradise", "Elite Entries",
            "Craftsman's Choice", "Modern Portals", "Heritage Doors"
        ));
        examples.put("HardwareType", List.of(
            "Pivot", "Sliding", "Pocket", "Barn",
            "French", "Bi-fold", "Dutch", "Smart Lock"
        ));
        examples.put("Color", List.of(
            "WHITE", "BROWN", "BLACK", "CUSTOM"
        ));
        examples.put("Size", List.of(
            "800x2000", "900x2000", "1000x2000", "CUSTOM"
        ));
        return examples;
    }
}
