package uz.pdp.mapper;

import org.mapstruct.*;
import uz.pdp.dto.FurnitureDoorCreateDTO;
import uz.pdp.dto.FurnitureDoorResponseDTO;
import uz.pdp.entity.FurnitureDoor;

/**
 * Mapper for converting between Door entities and DTOs.
 * The door's personal translator - making sure entities and DTOs speak the same
 * language! 🗣️
 * 
 * Warning: May cause occasional door-to-DTO transformation dizziness! 🌀
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FurnitureDoorMapper {

    /**
     * Converts DTO to Entity.
     * Like a door carpenter, but for objects! 🔨
     * 
     * @param dto The DTO to convert
     * @return A shiny new door entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "images", ignore = true)
    FurnitureDoor toEntity(FurnitureDoorCreateDTO dto);

    /**
     * Converts Entity to DTO.
     * Transforming doors like a magical doorway! ✨
     * 
     * @param entity The entity to convert
     * @return A presentable door DTO, ready for its API debut!
     */
    @Mapping(target = "imageUrls", source = "images")
    FurnitureDoorResponseDTO toDto(FurnitureDoor entity);

    /**
     * Updates an existing entity with data from DTO.
     * Like giving a door a makeover! 🎨
     * 
     * @param dto The source DTO
     * @param entity The entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "images", ignore = true)
    void updateEntityFromDto(FurnitureDoorCreateDTO dto, @MappingTarget FurnitureDoor entity);

    /**
     * After mapping, initialize collections if needed.
     */
    @AfterMapping
    default void initializeCollections(@MappingTarget FurnitureDoor entity) {
        if (entity.getImages() == null) {
            entity.setImages(new java.util.ArrayList<>());
        }
    }
}
