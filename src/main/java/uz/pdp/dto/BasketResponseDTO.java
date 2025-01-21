package uz.pdp.dto;

import lombok.Data;
import uz.pdp.entity.Basket;
import uz.pdp.entity.BasketItem;
import uz.pdp.entity.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for basket response to prevent infinite recursion.
 * 
 * Fun fact: This DTO is like a shopping receipt,
 * but without the "Thank you, come again!" at the bottom 🛍️
 */
@Data
public class BasketResponseDTO {
    private Long id;
    private UserDTO user;
    private List<BasketItemResponseDTO> items;
    private double totalPrice;

    @Data
    public static class UserDTO {
        private Long id;
        private String name;
        private String email;
        private String phone;
    }

    @Data
    public static class BasketItemResponseDTO {
        private Long id;
        private Long itemId;
        private String type;
        private String name;        // Item name
        private String imageUrl;    // Item image URL
        private int quantity;
        private double price;
        private double totalPrice;
    }

    public static BasketResponseDTO fromBasket(Basket basket) {
        BasketResponseDTO dto = new BasketResponseDTO();
        dto.setId(basket.getId());
        dto.setTotalPrice(basket.getTotalPrice());
        
        // Set user information
        if (basket.getUser() != null) {
            UserDTO userDto = new UserDTO();
            userDto.setId(basket.getUser().getId());
            userDto.setName(basket.getUser().getName());
            userDto.setEmail(basket.getUser().getEmail());
            userDto.setPhone(basket.getUser().getPhone());
            dto.setUser(userDto);
        }
        
        dto.setItems(basket.getItems().stream()
            .map(item -> {
                BasketItemResponseDTO itemDto = new BasketItemResponseDTO();
                itemDto.setId(item.getId());
                itemDto.setItemId(item.getItemId());
                itemDto.setType(item.getType().toString());
                itemDto.setQuantity(item.getQuantity());
                itemDto.setPrice(item.getPrice());
                itemDto.setTotalPrice(item.getPrice() * item.getQuantity());
                itemDto.setName(item.getName());
                itemDto.setImageUrl(item.getImage());
                return itemDto;
            })
            .collect(Collectors.toList()));
        
        return dto;
    }
}
