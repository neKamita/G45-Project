package uz.pdp.mapper;

import org.springframework.stereotype.Component;
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
@Component // This annotation is crucial for Spring to detect the mapper
public class FurnitureDoorMapper {

    /**
     * Converts DTO to Entity.
     * Like a door carpenter, but for objects! 🔨
     * 
     * @param dto The DTO to convert
     * @return A shiny new door entity
     */
    public FurnitureDoor toEntity(FurnitureDoorCreateDTO dto) {
        if (dto == null) {
            return null;
        }

        FurnitureDoor entity = new FurnitureDoor();
        entity.setName(dto.getName());
        entity.setMaterial(dto.getMaterial());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setDimensions(dto.getDimensions());
        entity.setStockQuantity(dto.getStockQuantity());
        entity.setFurnitureType(dto.getFurnitureType());
        return entity;
    }

    /**
     * Converts Entity to DTO.
     * Transforming doors like a magical doorway! ✨
     * 
     * @param entity The entity to convert
     * @return A presentable door DTO, ready for its API debut!
     */
    public FurnitureDoorResponseDTO toDto(FurnitureDoor entity) {
        if (entity == null) {
            return null;
        }

        FurnitureDoorResponseDTO dto = new FurnitureDoorResponseDTO();
        dto.setId(entity.getId()); // The door's social security number 🔢
        dto.setName(entity.getName()); // What we call this beauty
        dto.setMaterial(entity.getMaterial()); // What it's made of (hopefully not cardboard!)
        dto.setDescription(entity.getDescription()); // Its Tinder bio
        dto.setPrice(entity.getPrice()); // The price of beauty 💰
        dto.setDimensions(entity.getDimensions()); // How much space it needs to show off
        dto.setStockQuantity(entity.getStockQuantity()); // How many twins it has
        dto.setFurnitureType(entity.getFurnitureType()); // What kind of door star it is
        dto.setImageUrls(entity.getImageUrls()); // Its photo album 📸
        return dto;
    }
}
