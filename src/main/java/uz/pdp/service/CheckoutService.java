package uz.pdp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.dto.CheckoutDTO;
import uz.pdp.dto.CheckoutHistoryDTO;
import uz.pdp.entity.*;
import uz.pdp.enums.ItemType;
import uz.pdp.enums.OrderType;
import uz.pdp.payload.EntityResponse;
import uz.pdp.repository.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * Service for handling checkout operations.
 * Where doors, mouldings, and furniture find their forever homes! 🏠✨
 */
@Service
@RequiredArgsConstructor
public class CheckoutService {
    private final DoorRepository doorRepository;
    private final MouldingRepository mouldingRepository;
    private final FurnitureDoorRepository furnitureDoorRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);

    /**
     * Process a checkout request for any type of item.
     * Every door deserves a happy ending! 
     *
     * @param dto The checkout information
     * @return Response containing order confirmation
     * @throws IllegalArgumentException if item not found or invalid request
     */
    @Transactional
    public EntityResponse<String> processCheckout(CheckoutDTO dto) {
        try {
            // Get current user if authenticated, otherwise create a temporary user
            User user = null;
            try {
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                if (!"anonymousUser".equals(username)) {
                    user = userRepository.findByName(username)
                        .orElse(null);
                }
            } catch (Exception e) {
                log.debug("No authenticated user found, proceeding with guest checkout");
            }

            // For guest checkout or if user not found
            if (user == null) {
                user = new User();
                user.setName(dto.getCustomerName());
                user.setEmail(dto.getEmail());
                user.setPhone(dto.getPhoneNumber());
                // Save temporary user
                user = userRepository.save(user);
                log.info("Created temporary user for guest checkout: {}", user.getEmail());
            }

            // Get seller email based on item type
            String sellerEmail = switch (dto.getItemType()) {
                case DOOR -> processDoorCheckout(dto);
                case MOULDING -> processMouldingCheckout(dto);
                case DOOR_ACCESSORY -> processDoorAccessoryCheckout(dto);
                default -> throw new IllegalArgumentException("Invalid item type");
            };

            // Create and save order
            Order order = new Order();
            order.setUser(user);
            order.setItemId(dto.getItemId());
            order.setItemType(dto.getItemType());
            order.setItemName(getItemName(dto.getItemId(), dto.getItemType()));
            order.setPrice(getItemPrice(dto.getItemId(), dto.getItemType()));
            order.setQuantity(1); // Default to 1 for now
            order.setStatus(Order.OrderStatus.PENDING);
            order.setOrderDate(java.time.ZonedDateTime.now());
            order.setDeliveryAddress(dto.getDeliveryAddress());
            order.setContactPhone(dto.getPhoneNumber());
            
            // Set customer details
            order.setCustomerName(dto.getCustomerName());
            order.setEmail(dto.getEmail());
            
            // Set order type (default to PURCHASE if not specified)
            order.setOrderType(dto.getOrderType() != null ? dto.getOrderType() : OrderType.FULL_SET);
            
            // Set optional fields if provided
            if (dto.getComment() != null) {
                order.setComment(dto.getComment());
            }
            if (dto.getPreferredDeliveryTime() != null) {
                order.setPreferredDeliveryTime(dto.getPreferredDeliveryTime());
            }
            
            Order savedOrder = orderRepository.save(order);
            log.info("Created new order with ID: {} for user: {}", savedOrder.getId(), dto.getCustomerName());

            // Send email to seller
            String sellerMessage = String.format("""
                New order received!
                Order ID: %d
                Item Type: %s
                Item ID: %d
                Customer Name: %s
                Customer Email: %s
                Phone: %s
                Delivery Address: %s
                Comment: %s
                """, 
                savedOrder.getId(), dto.getItemType(), dto.getItemId(), dto.getCustomerName(),
                dto.getEmail(), dto.getPhoneNumber(), dto.getDeliveryAddress(),
                dto.getComment() != null ? dto.getComment() : "No comment"
            );
            
            try {
                emailService.sendHtmlEmail(sellerEmail, "New Order Received!", sellerMessage);
                log.info("Sent order notification email to seller: {}", sellerEmail);
            } catch (Exception e) {
                log.error("Failed to send email to seller: {}", e.getMessage());
                // Don't throw exception, continue with order processing
            }

            // Send confirmation to customer
            String customerMessage = """
                Thank you for your order! 
                We've notified the seller and they will contact you soon.
                
                Order Details:
                """.concat(sellerMessage);
            
            try {
                emailService.sendHtmlEmail(dto.getEmail(), "Order Confirmation", customerMessage);
                log.info("Sent order confirmation email to customer: {}", dto.getEmail());
            } catch (Exception e) {
                log.error("Failed to send confirmation email to customer: {}", e.getMessage());
                // Don't throw exception, continue with order processing
            }

            return new EntityResponse<>("Order placed successfully! Check your email for confirmation. 🎉", true, "ORDER_PLACED");
        } catch (Exception e) {
            log.error("Error processing checkout: {}", e.getMessage(), e);
            return new EntityResponse<>("Failed to process order. Please try again. 🔄", false, null);
        }
    }

    private String getItemName(Long itemId, ItemType type) {
        return switch (type) {
            case DOOR -> doorRepository.findById(itemId)
                .map(Door::getName)
                .orElse("Unknown Door");
            case MOULDING -> mouldingRepository.findById(itemId)
                .map(Moulding::getTitle)
                .orElse("Unknown Moulding");
            case DOOR_ACCESSORY -> furnitureDoorRepository.findById(itemId)
                .map(FurnitureDoor::getName)
                .orElse("Unknown Accessory");
            default -> "Unknown Item";
        };
    }

    private Double getItemPrice(Long itemId, ItemType type) {
        return switch (type) {
            case DOOR -> doorRepository.findById(itemId)
                .map(Door::getPrice)
                .orElse(0.0);
            case MOULDING -> mouldingRepository.findById(itemId)
                .map(Moulding::getPrice)
                .orElse(0.0);
            case DOOR_ACCESSORY -> furnitureDoorRepository.findById(itemId)
                .map(FurnitureDoor::getPrice)
                .orElse(0.0);
            default -> 0.0;
        };
    }

    private String processDoorCheckout(CheckoutDTO dto) {
        Door door = doorRepository.findById(dto.getItemId())
            .orElseThrow(() -> new IllegalArgumentException("Door not found"));
        return door.getSeller().getEmail();
    }

    private String processMouldingCheckout(CheckoutDTO dto) {
        Moulding moulding = mouldingRepository.findById(dto.getItemId())
            .orElseThrow(() -> new IllegalArgumentException("Moulding not found"));
        return moulding.getUser().getEmail();
    }

    private String processDoorAccessoryCheckout(CheckoutDTO dto) {
        FurnitureDoor accessory = furnitureDoorRepository.findById(dto.getItemId())
            .orElseThrow(() -> new IllegalArgumentException("Door accessory not found"));
        return accessory.getUser().getEmail();
    }

    /**
     * Get checkout history for the current user.
     * Let's take a walk down memory lane and see what you've ordered! 🛍️✨
     *
     * @return List of checkout history entries
     */
    public EntityResponse<List<CheckoutHistoryDTO>> getCheckoutHistory() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            log.debug("Fetching checkout history for user: {}", username);
            
            Optional<User> userOptional = userRepository.findByName(username);
            
            if (userOptional.isEmpty()) {
                log.warn("User not found with username: {}", username);
                return EntityResponse.error("Oops! We couldn't find your account. Please try logging in again. 🔑");
            }
            
            User user = userOptional.get();
            List<CheckoutHistoryDTO> history = orderRepository.findAllByUserIdOrderByOrderDateDesc(user.getId())
                    .stream()
                    .map(order -> CheckoutHistoryDTO.builder()
                            .id(order.getId())
                            .itemName(order.getItemName())
                            .itemType(order.getItemType())
                            .price(order.getPrice())
                            .quantity(order.getQuantity())
                            .status(order.getStatus().toString())
                            .checkoutTime(order.getOrderDate().toLocalDateTime())
                            .deliveryAddress(order.getDeliveryAddress())
                            .contactPhone(order.getContactPhone())
                            .build())
                    .collect(Collectors.toList());

            if (history.isEmpty()) {
                return EntityResponse.success("Looks like you haven't ordered anything yet. Time to start shopping! 🛍️", Collections.emptyList());
            }

            return EntityResponse.success("Here's your order history! 🛍️", history);
        } catch (Exception e) {
            log.error("Error fetching checkout history: ", e);
            return EntityResponse.error("Something went wrong while fetching your order history. Please try again later. 🔄");
        }
    }
}
