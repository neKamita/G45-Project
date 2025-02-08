package uz.pdp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.entity.Order;
import uz.pdp.entity.Order.OrderStatus;
import uz.pdp.enums.ItemType;
import uz.pdp.enums.OrderType;

import java.time.ZonedDateTime;

/**
 * DTO for returning order information without sensitive user data.
 * Every order has a story to tell! 🛍️✨
 * 
 * Key Features:
 * - Basic order details (ID, type, status)
 * - Item information (name, type, price)
 * - Customer contact info
 * - Delivery preferences
 * - Special instructions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
    private Long id;
    private Long userId;  // Only return the user ID, not full details
    
    // Item details
    private Long itemId;
    private ItemType itemType;
    private String itemName;
    private Double price;
    private Integer quantity;
    
    private OrderType orderType;
    private String customerName;
    private String email;
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
        
        // Set item details
        dto.setItemId(order.getItemId());
        dto.setItemType(order.getItemType());
        dto.setItemName(order.getItemName());
        dto.setPrice(order.getPrice());
        dto.setQuantity(order.getQuantity());
        
        dto.setOrderType(order.getOrderType());
        dto.setCustomerName(order.getCustomerName());
        dto.setEmail(order.getEmail());
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
