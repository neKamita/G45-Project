package uz.pdp.mapper;

import org.mapstruct.*;
import uz.pdp.dto.OrderDto;
import uz.pdp.entity.Order;
import uz.pdp.entity.User;

/**
 * Mapper for converting between Order entities and DTOs.
 *      
 * Fun fact: This mapper is like a door's personal assistant - 
 * taking care of all the delivery details! 📦✨
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    /**
     * Converts an Order entity to an OrderDTO.
     * 
     * @param order The order entity to convert
     * @return The converted DTO
     * 
     * Like creating a door's shipping manifest! 📝
     */
    OrderDto toDto(Order order);

    /**
     * Converts an OrderDTO to an Order entity.
     * 
     * @param dto The DTO to convert
     * @return The converted entity
     * 
     * Time to prepare that item for its journey! 🚚
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "orderDate", expression = "java(java.time.ZonedDateTime.now())")
    @Mapping(target = "status", constant = "PENDING")
    Order toEntity(OrderDto dto);

    /**
     * Updates an existing Order entity with DTO data.
     * 
     * @param dto The source DTO
     * @param order The order entity to update
     * 
     * Like updating a delivery instructions! 📋
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntityFromDto(OrderDto dto, @MappingTarget Order order);

    /**
     * Sets the user reference in the order.
     * 
     * @param order The order to update
     * @param user The user to set
     */
    @AfterMapping
    default void setUser(@MappingTarget Order order, @Context User user) {
        if (user != null) {
            order.setUser(user);
        }
    }
}
