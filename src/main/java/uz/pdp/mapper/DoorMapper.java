package uz.pdp.mapper;

import org.mapstruct.*;
import uz.pdp.dto.DoorDto;
import uz.pdp.entity.Door;

/**
 * Mapper for converting between Door entities and DTOs.
 * 
 * Think of this mapper as a door's personal stylist - making sure it looks 
 * fabulous whether it's heading to the database or strutting down the API runway! 
 * 
 * Key Features:
 * - Entity to DTO transformation
 * - DTO to Entity creation
 * - Existing door updates
 * 
 * @version 1.0
 * @since 2025-01-24
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DoorMapper {

    /**
     * Converts a Door entity to a DoorDTO.
     * 
     * @param door The door entity to convert
     * @return The converted DTO, ready for its API debut!
     * 
     * Like taking a door from the warehouse to the showroom! 
     */
    @Mapping(target = "status", expression = "java(door.getStatus().toString())")
    DoorDto toDto(Door door);

    /**
     * Converts a DoorDTO to a Door entity.
     * 
     * @param dto The DTO to convert
     * @return The converted entity
     * 
     * Like bringing a door blueprint to life! 
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "status", constant = "AVAILABLE")
    @Mapping(target = "baseModelId", ignore = true)
    @Mapping(target = "isBaseModel", constant = "false")
    @Mapping(target = "availableColors", ignore = true)
    Door toEntity(DoorDto dto);

    /**
     * Updates an existing Door entity with DTO data.
     * 
     * @param dto The source DTO
     * @param door The door entity to update
     * 
     * Like giving a door a makeover! 
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "baseModelId", ignore = true)
    @Mapping(target = "isBaseModel", ignore = true)
    @Mapping(target = "availableColors", ignore = true)
    void updateEntityFromDto(DoorDto dto, @MappingTarget Door door);

    /**
     * After mapping, initialize collections and calculate final price.
     * 
     * @param door The mapped door entity
     */
    @AfterMapping
    default void afterMapping(@MappingTarget Door door) {
        if (door.getImages() == null) {
            door.setImages(new java.util.ArrayList<>());
        }
        if (door.getAvailableColors() == null) {
            door.setAvailableColors(new java.util.HashSet<>());
        }
        door.calculateFinalPrice();
    }
}
