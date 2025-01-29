package uz.pdp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.dto.OrderDto.OrderType;
import uz.pdp.entity.Door;
import uz.pdp.entity.Order;
import uz.pdp.entity.Order.OrderStatus;

import java.time.ZonedDateTime;

/**
 * DTO for returning order information without sensitive user data.
 * Just the essential details about your door's journey to its new home! 🚪✨
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
    private Long id;
    private Long userId;  // Only return the user ID, not full details
    private Door door;
    private OrderType orderType;
    private String customerName;
    private String deliveryAddress;
    private String contactPhone;
    private ZonedDateTime orderDate;
    private ZonedDateTime preferredDeliveryTime;
    private String comment;
    private String installationNotes;
    private String deliveryNotes;
    private OrderStatus status;

    public static OrderResponseDTO fromOrder(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setDoor(order.getDoor());
        dto.setOrderType(order.getOrderType());
        dto.setCustomerName(order.getCustomerName());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setContactPhone(order.getContactPhone());
        dto.setOrderDate(order.getOrderDate());
        dto.setPreferredDeliveryTime(order.getPreferredDeliveryTime());
        dto.setComment(order.getComment());
        dto.setInstallationNotes(order.getInstallationNotes());
        dto.setDeliveryNotes(order.getDeliveryNotes());
        dto.setStatus(order.getStatus());
        return dto;
    }
}
