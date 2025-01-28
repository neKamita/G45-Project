package uz.pdp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.entity.*;
import uz.pdp.enums.ItemType;
import uz.pdp.repository.BasketItemRepository;
import uz.pdp.repository.BasketRepository;
import uz.pdp.repository.UserRepository;
import uz.pdp.exception.GlobalExceptionHandler.FurnitureDoorNotFoundException;
import uz.pdp.dto.BasketItemDTO;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service for managing shopping basket operations.
 * 
 * Fun fact: This service is like a digital shopping assistant,
 * but it won't judge your door choices! 
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BasketService {
    private final BasketRepository basketRepository;
    private final BasketItemRepository basketItemRepository;
    private final DoorService doorService;
    private final FurnitureDoorService furnitureDoorService;
    private final MouldingService mouldingService;
    private final UserRepository userRepository;

    /**
     * Get the current user from the security context.
     * @throws IllegalStateException if no user is authenticated
     */
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String name = ((UserDetails) principal).getUsername();
            return userRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("User not found: " + name));
        }   
        throw new IllegalStateException("No authenticated user found");
    }

    /**
     * Get the current user's basket.
     * Creates a new basket if one doesn't exist.
     */
    public Basket getBasket() {
        User user = getCurrentUser();
        return basketRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Basket basket = new Basket();
                    basket.setUser(user);
                    return basketRepository.save(basket);
                });
    }

    /**
     * Add an item to the basket using DTO.
     * @param itemDTO The item to add
     * @return Updated basket
     */
    public Basket addItem(BasketItemDTO itemDTO) {
        return addItem(itemDTO.getItemId(), itemDTO.getType(), itemDTO.getQuantity());
    }

    /**
     * Add an item to the basket.
     * @param itemId ID of the item to add
     * @param type Type of item (DOOR or ACCESSORY)
     * @param quantity Quantity to add
     * @return Updated basket
     */
    public Basket addItem(Long itemId, ItemType type, int quantity) {
        Basket basket = getBasket();
        
        // Get item details based on type
        String name;
        String image;
        double price;
        
        if (type == ItemType.DOOR) {
            Door door = doorService.getDoorById(itemId);
            name = door.getName();
            image = door.getImages().isEmpty() ? null : door.getImages().get(0);
            price = door.getFinalPrice() != null ? door.getFinalPrice() : door.getPrice();
        } else if (type == ItemType.MOULDING) {
            Optional<Moulding> moulding = mouldingService.getMouldingById(itemId);
            if (moulding.isEmpty()) {
                throw new IllegalArgumentException("Moulding not found: " + itemId);
            }
            Moulding moulding1 = moulding.get();
            name = moulding1.getTitle();
            image = moulding1.getImagesUrl().isEmpty() ? null : moulding1.getImagesUrl().get(0);
            price = moulding1.getPrice();
        } else {
            FurnitureDoor accessory = furnitureDoorService.getById(itemId)
                .orElseThrow(() -> new FurnitureDoorNotFoundException(itemId));
            name = accessory.getName();
            image = accessory.getImages().isEmpty() ? null : accessory.getImages().get(0);
            price = accessory.getPrice();
        }
        
        // Check if item already exists in basket
        BasketItem existingItem = basket.getItems().stream()
                .filter(item -> Objects.equals(item.getItemId(), itemId) && item.getType() == type)
                .findFirst()
                .orElse(null);
        
        if (existingItem != null) {
            // Update quantity if item exists
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            // Create new basket item
            BasketItem basketItem = new BasketItem();
            basketItem.setBasket(basket);
            basketItem.setItemId(itemId);
            basketItem.setType(type);
            basketItem.setQuantity(quantity);
            basketItem.setPrice(price);
            basketItem.setName(name);
            basketItem.setImage(image);
            basket.getItems().add(basketItem);
        }
        
        return basketRepository.save(basket);
    }

    /**
     * Remove an item from the basket.
     */
    public void removeBasketItem(Long basketItemId) {
        BasketItem basketItem = basketItemRepository.findById(basketItemId)
                .orElseThrow(() -> new IllegalArgumentException("Basket item not found: " + basketItemId));
        
        if (!Objects.equals(basketItem.getBasket().getId(), getCurrentUserBasket().getId())) {
            throw new IllegalStateException("Cannot remove item from another user's basket");
        }

        basketItemRepository.deleteBasketItemById(basketItemId);
        log.info("Successfully removed basket item {} from basket {}", basketItemId, basketItem.getBasket().getId());
    }

    /**
     * Update the quantity of an item in the basket.
     */
    @Transactional
    public Basket updateItemQuantity(Long itemId, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        BasketItem item = basketItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Basket item not found"));

        if (quantity == 0) {
            // Delete using native query
            basketItemRepository.deleteById(itemId);
            return getBasket();
        }

        item.setQuantity(quantity);
        return getBasket();
    }

    /**
     * Clear all items from the basket.
     * 
     * Fun fact: This is like a digital spring cleaning for your door collection!
     * Sometimes items play hide and seek, but we'll find them all! 
     */
    @Transactional
    public void clearBasket() {
        Basket basket = getCurrentUserBasket();
        log.info("Clearing basket with ID: {} containing {} items", basket.getId(), basket.getItems().size());
        basketItemRepository.deleteAllByBasketId(basket.getId());
        log.info("Successfully cleared all items from basket {}", basket.getId());
    }

    /**
     * Get the price of an item based on its type.
     */
    private double getItemPrice(Long itemId, ItemType type) {
        return switch (type) {
            case DOOR -> doorService.getDoorById(itemId).getPrice();
            case MOULDING -> mouldingService.getMouldingById(itemId).get().getPrice();
            case ACCESSORY -> furnitureDoorService.getById(itemId)
                .orElseThrow(() -> new FurnitureDoorNotFoundException(itemId))
                .getPrice();
        };
    }

    private Basket getCurrentUserBasket() {
        User user = getCurrentUser();
        return basketRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("User's basket not found"));
    }
}   
