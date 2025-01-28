package uz.pdp.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import uz.pdp.dto.OrderDto;
import uz.pdp.entity.Door;
import uz.pdp.entity.Order;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-01-28T14:54:58+0500",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.41.0.z20250115-2156, environment: Java 21.0.5 (Eclipse Adoptium)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public OrderDto toDto(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderDto orderDto = new OrderDto();

        orderDto.setDoorId( orderDoorId( order ) );
        orderDto.setComment( order.getComment() );
        orderDto.setContactPhone( order.getContactPhone() );
        orderDto.setCustomerName( order.getCustomerName() );
        orderDto.setDeliveryAddress( order.getDeliveryAddress() );
        orderDto.setDeliveryNotes( order.getDeliveryNotes() );
        orderDto.setEmail( order.getEmail() );
        orderDto.setInstallationNotes( order.getInstallationNotes() );
        orderDto.setOrderType( order.getOrderType() );
        orderDto.setPreferredDeliveryTime( order.getPreferredDeliveryTime() );

        return orderDto;
    }

    @Override
    public Order toEntity(OrderDto dto) {
        if ( dto == null ) {
            return null;
        }

        Order order = new Order();

        order.setComment( dto.getComment() );
        order.setContactPhone( dto.getContactPhone() );
        order.setCustomerName( dto.getCustomerName() );
        order.setDeliveryAddress( dto.getDeliveryAddress() );
        order.setDeliveryNotes( dto.getDeliveryNotes() );
        order.setEmail( dto.getEmail() );
        order.setInstallationNotes( dto.getInstallationNotes() );
        order.setOrderType( dto.getOrderType() );
        order.setPreferredDeliveryTime( dto.getPreferredDeliveryTime() );

        order.setOrderDate( java.time.ZonedDateTime.now() );
        order.setStatus( Order.OrderStatus.PENDING );

        return order;
    }

    @Override
    public void updateEntityFromDto(OrderDto dto, Order order) {
        if ( dto == null ) {
            return;
        }

        order.setComment( dto.getComment() );
        order.setContactPhone( dto.getContactPhone() );
        order.setCustomerName( dto.getCustomerName() );
        order.setDeliveryAddress( dto.getDeliveryAddress() );
        order.setDeliveryNotes( dto.getDeliveryNotes() );
        order.setEmail( dto.getEmail() );
        order.setInstallationNotes( dto.getInstallationNotes() );
        order.setOrderType( dto.getOrderType() );
        order.setPreferredDeliveryTime( dto.getPreferredDeliveryTime() );
    }

    private Long orderDoorId(Order order) {
        if ( order == null ) {
            return null;
        }
        Door door = order.getDoor();
        if ( door == null ) {
            return null;
        }
        Long id = door.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
