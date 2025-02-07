package uz.pdp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.dto.CheckoutDTO;
import uz.pdp.entity.*;
import uz.pdp.enums.ItemType;
import uz.pdp.payload.EntityResponse;
import uz.pdp.repository.*;

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
        String sellerEmail = switch (dto.getItemType()) {
            case DOOR -> processDoorCheckout(dto);
            case MOULDING -> processMouldingCheckout(dto);
            case DOOR_ACCESSORY -> processDoorAccessoryCheckout(dto);
            default -> throw new IllegalArgumentException("Invalid item type");
        };

        // Send email to seller
        String sellerMessage = String.format("""
            New order received!
            Item Type: %s
            Item ID: %d
            Customer Name: %s
            Customer Email: %s
            Phone: %s
            Delivery Address: %s
            Comment: %s
            """, 
            dto.getItemType(), dto.getItemId(), dto.getCustomerName(),
            dto.getEmail(), dto.getPhoneNumber(), dto.getDeliveryAddress(),
            dto.getComment() != null ? dto.getComment() : "No comment"
        );
        emailService.sendHtmlEmail(sellerEmail, "New Order Received!", sellerMessage);

        // Send confirmation to customer
        String customerMessage = """
            Thank you for your order! 
            We've notified the seller and they will contact you soon.
            
            Order Details:
            """.concat(sellerMessage);
        emailService.sendHtmlEmail(dto.getEmail(), "Order Confirmation", customerMessage);

        return new EntityResponse<>("Order placed successfully! Check your email for confirmation.", true, "ORDER_PLACED");
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
}
