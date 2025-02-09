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
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px">
                    <div style="background:linear-gradient(135deg,#4a90e2 0%%,#357abd 100%%);color:white;padding:20px;border-radius:10px 10px 0 0;text-align:center">
                        <h1 style="margin:0">🚪 New Door Order!</h1>
                    </div>
                    <div style="background:#fff;padding:20px;border-radius:0 0 10px 10px;box-shadow:0 2px 5px rgba(0,0,0,0.1)">
                        <div style="background:#f8f9fa;padding:15px;border-radius:5px;margin:15px 0">
                            <p><strong style="color:#4a90e2">Order ID:</strong> %d</p>
                            <p><strong style="color:#4a90e2">Item:</strong> %s #%d</p>
                            <p><strong style="color:#4a90e2">Customer:</strong> %s</p>
                            <p><strong style="color:#4a90e2">Email:</strong> %s</p>
                            <p><strong style="color:#4a90e2">Phone:</strong> %s</p>
                            <p><strong style="color:#4a90e2">Delivery To:</strong> %s</p>
                            <p><strong style="color:#4a90e2">Notes:</strong> %s</p>
                        </div>
                        <p style="text-align:center;color:#666">Please process this order as soon as possible. The customer is waiting! 🏃‍♂️</p>
                    </div>
                </div>
                """, 
                savedOrder.getId(), dto.getItemType(), dto.getItemId(), dto.getCustomerName(),
                dto.getEmail(), dto.getPhoneNumber(), dto.getDeliveryAddress(),
                dto.getComment() != null ? dto.getComment() : "No special instructions"
            );
            
            try {
                emailService.sendHtmlEmail(sellerEmail, "New Order Received!", sellerMessage);
                log.info("Sent order notification email to seller: {}", sellerEmail);
            } catch (Exception e) {
                log.error("Failed to send email to seller: {}", e.getMessage());
                // Don't throw exception, continue with order processing
            }

            // Send confirmation to customer
            String customerMessage = String.format("""
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px">
                    <div style="background:linear-gradient(135deg,#4a90e2 0%%,#357abd 100%%);color:white;padding:30px;border-radius:10px 10px 0 0;text-align:center">
                        <h1 style="margin:0">🎉 Your Door is on its Way!</h1>
                        <p style="margin:10px 0 0">Order #%d</p>
                    </div>
                    <div style="background:#fff;padding:30px;border-radius:0 0 10px 10px;box-shadow:0 2px 5px rgba(0,0,0,0.1)">
                        <div style="font-size:24px;color:#4a90e2;text-align:center;margin:20px 0">
                            Thank you for your order, %s!
                        </div>
                        
                        <p style="text-align:center">Your door journey has begun! Here's what happens next:</p>
                        
                        <div style="background:#e8f4ff;padding:20px;border-radius:8px;margin:20px 0">
                            <div style="display:flex;align-items:center;margin:10px 0">
                                <span style="font-size:24px;margin-right:15px">✅</span>
                                <span>Order confirmed and processing</span>
                            </div>
                            <div style="display:flex;align-items:center;margin:10px 0">
                                <span style="font-size:24px;margin-right:15px">📞</span>
                                <span>Seller will contact you soon</span>
                            </div>
                            <div style="display:flex;align-items:center;margin:10px 0">
                                <span style="font-size:24px;margin-right:15px">🚚</span>
                                <span>Delivery scheduling</span>
                            </div>
                            <div style="display:flex;align-items:center;margin:10px 0">
                                <span style="font-size:24px;margin-right:15px">🏠</span>
                                <span>Installation and setup</span>
                            </div>
                        </div>

                        <div style="background:#f8f9fa;padding:20px;border-radius:8px;margin:20px 0">
                            <h3 style="margin-top:0">Order Summary</h3>
                            <p><strong style="color:#4a90e2">Item:</strong> %s</p>
                            <p><strong style="color:#4a90e2">Delivery To:</strong> %s</p>
                            <p><strong style="color:#4a90e2">Contact:</strong> %s</p>
                            <p><strong style="color:#4a90e2">Preferred Time:</strong> %s</p>
                            <p><strong style="color:#4a90e2">Notes:</strong> %s</p>
                        </div>

                        <div style="text-align:center;margin-top:30px;color:#666">
                            <p>Need help? Have questions? We're here for you!</p>
                            <p>🚪 Thank you for choosing us for your door needs! ✨</p>
                        </div>
                    </div>
                </div>
                """,
                savedOrder.getId(),
                dto.getCustomerName(),
                dto.getItemType(),
                dto.getDeliveryAddress(),
                dto.getPhoneNumber(),
                dto.getPreferredDeliveryTime() != null ? dto.getPreferredDeliveryTime().toString() : "To be scheduled",
                dto.getComment() != null ? dto.getComment() : "No special instructions"
            );
            
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
