package uz.pdp.mapper;

import org.mapstruct.*;
import uz.pdp.dto.DoorDto;
import uz.pdp.dto.DoorResponseDTO;
import uz.pdp.entity.Door;
import uz.pdp.entity.Category;
import uz.pdp.dto.CategoryDTO;

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
     * Converts a Door entity to a DoorResponseDTO.
     * Making sure our door looks its best for the response! 
     *
     * @param door The door entity to convert
     * @return The response DTO with all the door's glamour shots
     */
    @Mapping(target = "category", expression = "java(mapCategory(door.getCategory()))")
    DoorResponseDTO toResponseDto(Door door);

    /**
     * Maps a Category entity to CategoryDTO, handling null and lazy loading.
     * 
     * @param category The category to map
     * @return Mapped CategoryDTO
     */
    default CategoryDTO mapCategory(Category category) {
        if (category == null) {
            return null;
        }
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }

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
