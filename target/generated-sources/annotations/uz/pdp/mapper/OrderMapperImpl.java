package uz.pdp.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import uz.pdp.dto.OrderDto;
import uz.pdp.entity.Order;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-02-08T08:33:02+0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.14 (Amazon.com Inc.)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public OrderDto toDto(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderDto orderDto = new OrderDto();

        orderDto.setOrderType( order.getOrderType() );
        orderDto.setCustomerName( order.getCustomerName() );
        orderDto.setDeliveryAddress( order.getDeliveryAddress() );
        orderDto.setContactPhone( order.getContactPhone() );
        orderDto.setEmail( order.getEmail() );
        orderDto.setPreferredDeliveryTime( order.getPreferredDeliveryTime() );
        orderDto.setComment( order.getComment() );
        orderDto.setInstallationNotes( order.getInstallationNotes() );
        orderDto.setDeliveryNotes( order.getDeliveryNotes() );

        return orderDto;
    }

    @Override
    public Order toEntity(OrderDto dto) {
        if ( dto == null ) {
            return null;
        }

        Order order = new Order();

        order.setOrderType( dto.getOrderType() );
        order.setCustomerName( dto.getCustomerName() );
        order.setEmail( dto.getEmail() );
        order.setDeliveryAddress( dto.getDeliveryAddress() );
        order.setContactPhone( dto.getContactPhone() );
        order.setPreferredDeliveryTime( dto.getPreferredDeliveryTime() );
        order.setComment( dto.getComment() );
        order.setInstallationNotes( dto.getInstallationNotes() );
        order.setDeliveryNotes( dto.getDeliveryNotes() );

        order.setOrderDate( java.time.ZonedDateTime.now() );
        order.setStatus( Order.OrderStatus.PENDING );

        return order;
    }

    @Override
    public void updateEntityFromDto(OrderDto dto, Order order) {
        if ( dto == null ) {
            return;
        }

        order.setOrderType( dto.getOrderType() );
        order.setCustomerName( dto.getCustomerName() );
        order.setEmail( dto.getEmail() );
        order.setDeliveryAddress( dto.getDeliveryAddress() );
        order.setContactPhone( dto.getContactPhone() );
        order.setPreferredDeliveryTime( dto.getPreferredDeliveryTime() );
        order.setComment( dto.getComment() );
        order.setInstallationNotes( dto.getInstallationNotes() );
        order.setDeliveryNotes( dto.getDeliveryNotes() );
    }
}
