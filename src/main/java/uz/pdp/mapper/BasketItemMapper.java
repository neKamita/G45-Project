package uz.pdp.mapper;

import org.mapstruct.*;
import uz.pdp.dto.BasketItemDTO;
import uz.pdp.entity.BasketItem;

/**
 * Mapper for converting between BasketItem entities and DTOs.
 * 
 * Fun fact: This mapper is like a doorman at a fancy hotel - 
 * it transforms regular items into their fancy DTO form! 🚪✨
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BasketItemMapper {

    /**
     * Converts a BasketItem entity to a BasketItemDTO.
     * 
     * @param basketItem The entity to convert
     * @return The converted DTO
     * 
     * Like a door getting dressed up for a night out! 🎭
     */
    @Mapping(target = "imageUrl", source = "image")
    @Mapping(target = "totalPrice", expression = "java(calculateTotalPrice(basketItem))")
    BasketItemDTO toDto(BasketItem basketItem);

    /**
     * Converts a BasketItemDTO to a BasketItem entity.
     * 
     * @param dto The DTO to convert
     * @return The converted entity
     * 
     * Time to get back to business mode! 🏢
     */
    @Mapping(target = "image", source = "imageUrl")
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "basket", ignore = true)
    BasketItem toEntity(BasketItemDTO dto);

    /**
     * Updates an existing BasketItem entity with DTO data.
     * 
     * @param dto The source DTO
     * @param entity The entity to update
     * 
     * Like giving a door a fresh coat of paint! 🎨
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "basket", ignore = true)
    @Mapping(target = "image", source = "imageUrl")
    void updateEntityFromDto(BasketItemDTO dto, @MappingTarget BasketItem entity);

    /**
     * Calculates the total price for a basket item.
     * 
     * @param basketItem The basket item
     * @return The total price
     */
    default java.math.BigDecimal calculateTotalPrice(BasketItem basketItem) {
        if (basketItem == null) return null;
        return java.math.BigDecimal.valueOf(basketItem.getPrice() * basketItem.getQuantity());
    }
}
