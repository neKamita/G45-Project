package uz.pdp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.dto.PriceListRequestDto;
import uz.pdp.entity.Door;
import uz.pdp.entity.FurnitureDoor;
import uz.pdp.entity.Moulding;
import uz.pdp.enums.Color;
import uz.pdp.enums.Size;
import uz.pdp.payload.EntityResponse;
import uz.pdp.repository.DoorRepository;
import uz.pdp.repository.FurnitureDoorRepository;
import uz.pdp.repository.MouldingRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Service for generating and sending price lists.
 * 
 * The price whisperer - telling you how much those beautiful doors cost!
 */
@Service
@RequiredArgsConstructor
public class PriceListService {
    private final DoorRepository doorRepository;
    private final FurnitureDoorRepository furnitureDoorRepository;
    private final MouldingRepository mouldingRepository;
    private final EmailService emailService;

    public EntityResponse<?> generateAndSendPriceList(PriceListRequestDto request) {
        StringBuilder content = new StringBuilder();

        // Add header
        content.append("""
                    <div class="header">
                        <h1> Price List</h1>
                        <p>Generated on %s</p>
                    </div>
                """.formatted(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))));

        // Add item details
        content.append("<div class='section'>");
        switch (request.getItemType()) {
            case DOOR -> generateDoorPriceList(request.getItemId(), content);
            case DOOR_ACCESSORY -> generateFurniturePriceList(request.getItemId(), content);
            case MOULDING -> generateMouldingPriceList(request.getItemId(), content);
        }
        content.append("</div>");

        // Add customer information
        content.append("""
                    <div class="customer-info">
                        <h3> Customer Information</h3>
                        <p><strong>Name:</strong> %s</p>
                        <p><strong>Email:</strong> %s</p>
                        <p><strong>Phone:</strong> %s</p>
                    </div>
                    <div class="footer">
                        <p>Thank you for your interest in our products! </p>
                        <p>If you have any questions, please don't hesitate to contact us.</p>
                    </div>
                """.formatted(request.getName(), request.getEmail(), request.getPhone()));

        // Send email
        String subject = "Price List for " + request.getItemType() + " #" + request.getItemId();
        return emailService.sendHtmlEmail(request.getEmail(), subject, content.toString());
    }

    private void generateDoorPriceList(Long doorId, StringBuilder content) {
        Door door = doorRepository.findById(doorId)
                .orElseThrow(() -> new IllegalArgumentException("Door not found"));

        content.append("""
                    <h2 class="title"> %s</h2>
                    <div class="price">Base Price: $%.2f</div>
                    <p>%s</p>

                    <h3>Size Variants</h3>
                    <div class="variants">
                """.formatted(door.getName(), door.getPrice(), door.getDescription()));

        // Add size variants
        Arrays.stream(Size.values()).forEach(size -> {
            double variantPrice = door.getFinalPrice();
            content.append("""
                        <div class="variant">
                            <strong>%s</strong> (%dx%d mm)
                            <span class="price">$%.2f</span>
                        </div>
                    """.formatted(size.getDisplayName(), size.getWidth(), size.getHeight(), variantPrice));
        });

        content.append("</div><h3>Color Options</h3><div class='variants'>");

        // Add color variants
        Arrays.stream(Color.values()).forEach(color -> {
            double variantPrice = door.getFinalPrice() != null ? door.getFinalPrice()
                    : calculateDoorPriceForColor(door, color);
            content.append("""
                        <div class="variant">
                            <strong>%s</strong>
                            <span class="price">$%.2f</span>
                        </div>
                    """.formatted(color.getDisplayName(), variantPrice));
        });

        content.append("</div>");
    }

    private void generateFurniturePriceList(Long furnitureId, StringBuilder content) {
        FurnitureDoor furniture = furnitureDoorRepository.findById(furnitureId)
                .orElseThrow(() -> new IllegalArgumentException("Furniture not found"));

        content.append("""
                    <h2 class="title"> %s</h2>
                    <div class="price">Price: $%.2f</div>
                    <p>%s</p>

                    <div class="variant">
                        <strong>Material:</strong> %s
                    </div>

                    <div class="variant">
                        <strong>Stock Available:</strong> %d pieces
                    </div>
                """.formatted(
                furniture.getName(),
                furniture.getPrice(),
                furniture.getDescription(),
                furniture.getMaterial(),
                furniture.getStockQuantity()));
    }

    private void generateMouldingPriceList(Long mouldingId, StringBuilder content) {
        Moulding moulding = mouldingRepository.findById(mouldingId)
                .orElseThrow(() -> new IllegalArgumentException("Moulding not found"));

        content.append("""
                    <h2 class="title"> %s</h2>
                    <div class="price">Price: $%.2f</div>
                    <p>%s</p>

                    <div class="variant">
                        <strong>Length:</strong> %s mm
                    </div>
                """.formatted(
                moulding.getTitle(),
                moulding.getPrice(),
                moulding.getDescription(),
                moulding.getSize()));
    }

    private double calculateDoorPriceForColor(Door door, Color color) {
        double basePrice = door.getPrice();
        double colorFactor = color == Color.CUSTOM ? 1.3 : 1.0;
        return basePrice * colorFactor;
    }
}
