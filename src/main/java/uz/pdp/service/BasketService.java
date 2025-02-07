package uz.pdp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.entity.*;
import uz.pdp.enums.ItemType;
import uz.pdp.exception.GlobalExceptionHandler.FurnitureDoorNotFoundException;
import uz.pdp.dto.BasketItemDTO;
import uz.pdp.repository.BasketItemRepository;
import uz.pdp.repository.BasketRepository;
import uz.pdp.repository.UserRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service for managing shopping basket operations.
 * Now with turbocharged Redis caching! 🛒✨
 * Because your shopping cart should move at the speed of light!
 *
 * @version 1.0
 * @since 2025-01-17
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BasketService {
    private static final String BASKET_CACHE = "basket";
    private static final String USER_BASKET_CACHE = "user-basket";
    private static final String BASKET_ITEMS_CACHE = "basket-items";

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
     * Cached per user - because your cart is your castle! 🏰
     */
    //@Cacheable(value = USER_BASKET_CACHE, key = "#root.target.getCurrentUser().id")
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
     * Get a basket by ID.
     * Cached individually - every basket deserves VIP treatment! 🌟
     */
    //@Cacheable(value = BASKET_CACHE, key = "#id")
    public Basket getBasketById(Long id) {
        return basketRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Basket not found with id: " + id));
    }

    /**
     * Get basket items.
     * Cached per basket - because nobody likes a slow cart! 🏃‍♂️
     */
    //@Cacheable(value = BASKET_ITEMS_CACHE, key = "#basketId")
    public List<BasketItem> getBasketItems(Long basketId) {
        Basket basket = getBasketById(basketId);
        return basket.getItems();
    }

    /**
     * Add an item to the basket using DTO.
     * Updates caches because shopping waits for no one! 🛍️
     */

    public Basket addItem(BasketItemDTO itemDTO) {
        return addItem(itemDTO.getItemId(), itemDTO.getType(), itemDTO.getQuantity());
    }

    /**
     * Add an item to the basket.
     * Updates caches because shopping waits for no one! 🛍️
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
        } else if (type == ItemType.DOOR_ACCESSORY) {
            FurnitureDoor accessory = furnitureDoorService.getById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Door accessory not found: " + itemId));
            name = accessory.getName();
            image = accessory.getImages().isEmpty() ? null : accessory.getImages().get(0);
            price = accessory.getPrice();
        } else {
            throw new IllegalArgumentException("Invalid item type: " + type);
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
     * Cleans up caches like a neat freak! 🧹
     */

    public void removeBasketItem(Long basketItemId) {
        BasketItem basketItem = basketItemRepository.findById(basketItemId)
                .orElseThrow(() -> new IllegalArgumentException("Basket item not found: " + basketItemId));
        
        if (!Objects.equals(basketItem.getBasket().getId(), getBasket().getId())) {
            throw new IllegalStateException("Cannot remove item from another user's basket");
        }

        basketItemRepository.deleteBasketItemById(basketItemId);
        log.info("Successfully removed basket item {} from basket {}", basketItemId, basketItem.getBasket().getId());
    }

    /**
     * Update the quantity of an item in the basket.
     * Updates caches faster than you can say "checkout"! 💨
     */

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
     * Evicts all caches because sometimes you need a fresh start! 🌅
     */

    public void clearBasket() {
        Basket basket = getBasket();
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
            case DOOR_ACCESSORY -> furnitureDoorService.getById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Door accessory not found: " + itemId))
                .getPrice();
            default -> throw new IllegalArgumentException("Invalid item type: " + type);
        };
    }

    private Basket getCurrentUserBasket() {
        User user = getCurrentUser();
        return basketRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("User's basket not found"));
    }

    /**
     * Checkout specific items from the basket.
     * Like a VIP door service - only the items you want! 🎯
     *
     * @param basketItemIds List of basket item IDs to checkout
     * @return List of checked out items
     * @throws IllegalArgumentException if any item is not found or doesn't belong to user's basket
     */
    @Transactional
    public List<BasketItem> checkoutItems(List<Long> basketItemIds) {
        Basket userBasket = getCurrentUserBasket();
        List<BasketItem> itemsToCheckout = basketItemRepository.findAllById(basketItemIds);

        // Verify all items exist and belong to user's basket
        if (itemsToCheckout.size() != basketItemIds.size()) {
            throw new IllegalArgumentException("Some items were not found in the basket");
        }

        for (BasketItem item : itemsToCheckout) {
            if (!Objects.equals(item.getBasket().getId(), userBasket.getId())) {
                throw new IllegalArgumentException(
                    "Item " + item.getId() + " does not belong to the current user's basket"
                );
            }
        }

        // Process the checkout (you can add payment processing here)
        double totalAmount = itemsToCheckout.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();

        log.info("Processing checkout for {} items, total amount: ${}", 
            itemsToCheckout.size(), String.format("%.2f", totalAmount));

        // Remove checked out items from basket
        for (BasketItem item : itemsToCheckout) {
            basketItemRepository.delete(item); // Use delete instead of deleteById for better transactional behavior
            userBasket.getItems().remove(item); // Also remove from the basket's items collection
        }
        basketRepository.save(userBasket); // Save the updated basket

        log.info("Successfully checked out {} items from basket {}", 
            itemsToCheckout.size(), userBasket.getId());

        return itemsToCheckout;
    }
}   
