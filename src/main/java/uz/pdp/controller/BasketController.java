package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import uz.pdp.dto.BasketCheckoutDto;
import uz.pdp.dto.BasketResponseDTO;
import uz.pdp.dto.OrderDto;
import uz.pdp.entity.Basket;
import uz.pdp.entity.BasketItem;
import uz.pdp.entity.Order;
import uz.pdp.entity.User;
import uz.pdp.enums.ItemType;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.*;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for managing shopping basket operations.
 * Where doors go to hang out before finding their forever homes! 
 */
@Slf4j
@RestController
@RequestMapping("/api/basket")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SELLER')")
@Tag(name = "Basket Controller", description = "API endpoints for managing shopping basket")
public class BasketController {
    private final BasketService basketService;
    private final OrderService orderService;
    private final UserService UserService;
    private final DoorService doorService;
    private final FurnitureDoorService furnitureDoorService;

    /**
     * Get the current user's basket.
     * 
     * @return Response containing the user's basket
     * 
     *         Here's what you've been hoarding! No judgment, we all need doors sometimes...
     */
    @GetMapping
    @Operation(summary = "Get user's basket", description = "Retrieves the current user's shopping basket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Basket retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Basket not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - User not authenticated")
    })
    public ResponseEntity<EntityResponse<BasketResponseDTO>> getBasket() {
        BasketResponseDTO basket = BasketResponseDTO.fromBasket(basketService.getBasket());
        
        if (basket.getItems() == null || basket.getItems().isEmpty()) {
            return ResponseEntity.ok(EntityResponse.success(
                "Your basket is looking lonely!  Time to add some fabulous doors! ",
                basket
            ));
        }
        
        return ResponseEntity.ok(EntityResponse.success(
            "Here's what you've been hoarding! No judgment, we all need doors sometimes...",
            basket
        ));
    }

    /**
     * Update the quantity of an item in the basket.
     * 
     * @param itemId The ID of the basket item to update
     * @param quantity New quantity (set to 0 to remove the item)
     * @return Response containing the updated basket
     * 
     *         Quantity updated! 
     *         Math is fun when it involves doors!
     */
    @PatchMapping("/items/{itemId}")
    @Operation(summary = "Update item quantity", description = "Updates the quantity of an item in the basket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quantity updated successfully"),
            @ApiResponse(responseCode = "404", description = "Item not found in basket"),
            @ApiResponse(responseCode = "400", description = "Invalid quantity"),
            @ApiResponse(responseCode = "403", description = "Access denied - User not authenticated")
    })
    public ResponseEntity<BasketResponseDTO> updateItemQuantity(
            @Parameter(description = "ID of the basket item") @PathVariable Long itemId,
            @Parameter(description = "New quantity (0 to remove)") @RequestParam int quantity) {
        return ResponseEntity.ok(BasketResponseDTO.fromBasket(basketService.updateItemQuantity(itemId, quantity)));
    }

    /**
     * Remove a specific item from the basket.
     * 
     * @param itemId The ID of the basket item to remove
     * @return Response containing the updated basket
     * 
     *         Item removed! 
     *         It's not you, it's the basket...
     */
    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item", description = "Removes a specific item from the basket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item removed successfully"),
            @ApiResponse(responseCode = "404", description = "Item not found in basket"),
            @ApiResponse(responseCode = "403", description = "Access denied - User not authenticated")
    })
    public ResponseEntity<BasketResponseDTO> removeItem(
            @Parameter(description = "ID of the basket item") @PathVariable Long itemId) {
        return ResponseEntity.ok(BasketResponseDTO.fromBasket(basketService.updateItemQuantity(itemId, 0)));
    }

    /**
     * Clear all items from the basket.
     * 
     * @return Response indicating success
     * 
     *         Poof! All gone! 
     *         Your basket is now as empty as a doorway without a door...
     */
    @DeleteMapping
    @Operation(summary = "Clear basket", description = "Removes all items from the user's basket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Basket cleared successfully"),
            @ApiResponse(responseCode = "404", description = "Basket not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - User not authenticated")
        })
    public ResponseEntity<BasketResponseDTO> clearBasket() {
        basketService.clearBasket();
        return ResponseEntity.ok(BasketResponseDTO.fromBasket(basketService.getBasket()));
    }

    /**
     * Create orders from all items in the basket.
     * 
     * @param checkoutDto Checkout details including delivery information
     * @return Response containing the created orders
     * 
     *         Your doors are on their way! 
     *         Time to start measuring those doorframes... 🚪✨
     */
    @PostMapping("/checkout")
    @Operation(
        summary = "Checkout basket",
        description = "Creates orders for all items in the basket. User details are taken from the authenticated user."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Orders created successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid checkout data or empty basket"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Basket not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - User not authenticated"
        )
    })
    @Transactional
    public ResponseEntity<EntityResponse<List<Order>>> checkout(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Checkout details containing delivery information. User details will be taken from the authenticated user.",
            required = true
        )
        @Valid @RequestBody BasketCheckoutDto checkoutDto
    ) {
        try {
            Basket basket = basketService.getBasket();
            User currentUser = UserService.getCurrentUser();
            
            if (basket.getItems() == null || basket.getItems().isEmpty()) {
                return ResponseEntity.ok(EntityResponse.error(
                    "Your basket is as empty as a keyhole without a key! Add some doors first! 🔑",
                    Collections.emptyList()
                ));
            }
            
            // Pre-validate all items to ensure they can be ordered
            for (BasketItem item : basket.getItems()) {
                try {
                    // Validate item existence and availability
                    if (item.getType() == ItemType.DOOR) {
                        doorService.getDoorById(item.getItemId());
                    } else {
                        furnitureDoorService.getById(item.getItemId())
                            .orElseThrow(() -> new RuntimeException("Accessory not found"));
                    }
                } catch (Exception e) {
                    return ResponseEntity.ok(EntityResponse.error(
                        String.format("Item '%s' is no longer available. Please remove it from your basket. 🚫", 
                            item.getName()),
                        Collections.emptyList()
                    ));
                }
            }
            
            // Create order DTOs for all items
            List<OrderDto> orderDtos = basket.getItems().stream()
                .map(item -> {
                    OrderDto orderDto = new OrderDto();
                    orderDto.setDoorId(item.getItemId());
                    orderDto.setOrderType(checkoutDto.getOrderType());
                    
                    // Use authenticated user's information
                    orderDto.setCustomerName(currentUser.getName());
                    orderDto.setEmail(currentUser.getEmail());
                    orderDto.setContactPhone(currentUser.getPhone());
                    
                    // Use delivery information from checkout DTO
                    orderDto.setDeliveryAddress(checkoutDto.getDeliveryAddress());
                    orderDto.setPreferredDeliveryTime(checkoutDto.getPreferredDeliveryTime());
                    orderDto.setComment(checkoutDto.getComment());
                    orderDto.setInstallationNotes(checkoutDto.getInstallationNotes());
                    orderDto.setDeliveryNotes(checkoutDto.getDeliveryNotes());
                    
                    return orderDto;
                })
                .collect(Collectors.toList());
            
            // Create all orders in a single transaction
            EntityResponse<List<Order>> orderResponse = orderService.createOrders(String.valueOf(currentUser.getId()), orderDtos);
            
            if (orderResponse.isSuccess()) {
                // Clear the basket items using a bulk delete
                basketService.clearBasket();
                
                String successMessage = String.format(
                    "✅ Success! Your %d door(s) are on their way to %s! " +
                    "We'll contact you at %s when they're ready for delivery. " +
                    "Get those doorframes ready! 🚪✨",
                    orderResponse.getData().size(),
                    String.valueOf(currentUser.getId()), 
                    currentUser.getEmail()
                );
                
                return ResponseEntity.ok(EntityResponse.success(successMessage, orderResponse.getData()));
            } else {
                return ResponseEntity.ok(EntityResponse.error(
                    "Oops! " + orderResponse.getMessage() + " 🔧",
                    Collections.emptyList()
                ));
            }
            
        } catch (ObjectOptimisticLockingFailureException e) {
            // Handle concurrent modification
            log.error("Concurrent modification during checkout: {}", e.getMessage());
            return ResponseEntity.ok(EntityResponse.error(
                "Oops! Your basket was modified by another session. " +
                "Please review your basket and try again. 🔄",
                Collections.emptyList()
            ));
        } catch (Exception e) {
            log.error("Checkout failed: {}", e.getMessage(), e);
            return ResponseEntity.ok(EntityResponse.error(
                "Oops! Something went wrong while processing your order. " +
                "Please try again or contact support if the problem persists. 🛠️",
                Collections.emptyList()
            ));
        }
    }
}
