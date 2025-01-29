package uz.pdp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.entity.BasketItem;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for checkout operations.
 * Wraps up your purchase details with a bow! 🎁
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponseDTO {
    private List<BasketItem> checkedOutItems;
    private double totalAmount;
    private LocalDateTime checkoutTime;
    private int itemCount;

    public static CheckoutResponseDTO from(List<BasketItem> items) {
        double total = items.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();

        return new CheckoutResponseDTO(
            items,
            total,
            LocalDateTime.now(),
            items.size()
        );
    }
}
