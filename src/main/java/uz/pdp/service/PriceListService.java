package uz.pdp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.dto.PriceListRequestDto;
import uz.pdp.entity.Door;
import uz.pdp.entity.FurnitureDoor;
import uz.pdp.entity.Moulding;
import uz.pdp.payload.EntityResponse;
import uz.pdp.repository.DoorRepository;
import uz.pdp.repository.FurnitureDoorRepository;
import uz.pdp.repository.MouldingRepository;

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

    /**
     * Generates and sends a price list for the specified item.
     * 
     * @param request The price list request containing item details (DOOR, DOOR_ACCESSORY, MOULDING)
     * @return Response indicating success or failure
     * 
     * Knock knock! Who's there? Your personalized price list! 📊
     */
    public EntityResponse<String> generateAndSendPriceList(PriceListRequestDto request) {
        StringBuilder content = new StringBuilder();

        try {
            // Validate item type and generate appropriate price list
            switch (request.getItemType().toString().toUpperCase()) {
                case "DOOR" -> generateDoorPriceList(request.getItemId(), content);
                case "DOOR_ACCESSORY" -> generateFurniturePriceList(request.getItemId(), content);
                case "MOULDING" -> generateMouldingPriceList(request.getItemId(), content);
                default -> {
                    return EntityResponse.error("Invalid item type. Supported types are: DOOR, DOOR_ACCESSORY, MOULDING 🚫");
                }
            }

            // Add header with customer info
            content.append(String.format("""
                    <div class="header">
                        <h1> Your Custom Price List</h1>
                        <div class="customer-info">
                            <p><strong>Name:</strong> %s</p>
                            <p><strong>Email:</strong> %s</p>
                            <p><strong>Phone:</strong> %s</p>
                        </div>
                    </div>
                    <div class="content">
                    """, request.getName(), request.getEmail(), request.getPhone()));

            content.append("""
                    </div>
                    <div class="footer">
                        <p>Thank you for your interest in our products! </p>
                        <p>If you have any questions, please don't hesitate to contact us.</p>
                        <small> 2025 DoorShop - Where doors come to life! </small>
                    </div>
                    """);

            // Wrap content in HTML template with styles
            String htmlEmail = String.format("""
                    <!DOCTYPE html>
                    <html>
                        <head>
                            <meta charset="UTF-8">
                            <title>DoorShop Price List</title>
                            <style>
                                body {
                                    font-family: Arial, sans-serif;
                                    line-height: 1.6;
                                    color: #333;
                                    max-width: 800px;
                                    margin: 0 auto;
                                    padding: 20px;
                                }
                                .header {
                                    background-color: #f8f9fa;
                                    border-radius: 10px;
                                    padding: 20px;
                                    text-align: center;
                                    margin-bottom: 30px;
                                }
                                .header h1 {
                                    color: #2c3e50;
                                    margin-bottom: 20px;
                                }
                                .customer-info {
                                    background-color: #fff;
                                    border-radius: 8px;
                                    padding: 15px;
                                    margin-top: 20px;
                                    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                                }
                                .content {
                                    background-color: #fff;
                                    border-radius: 10px;
                                    padding: 20px;
                                    margin-bottom: 30px;
                                    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                                }
                                .footer {
                                    text-align: center;
                                    color: #666;
                                    padding: 20px;
                                    background-color: #f8f9fa;
                                    border-radius: 10px;
                                }
                                .price {
                                    font-size: 1.2em;
                                    color: #2c3e50;
                                    font-weight: bold;
                                    margin: 10px 0;
                                }
                                .variant {
                                    background-color: #f8f9fa;
                                    padding: 10px;
                                    margin: 5px 0;
                                    border-radius: 5px;
                                }
                            </style>
                        </head>
                        <body>
                            %s
                        </body>
                    </html>
                    """, content);

            // Send email
            String subject = " Price List for " + request.getItemType() + " #" + request.getItemId();
            emailService.sendHtmlEmail(request.getEmail(), subject, htmlEmail);
            return EntityResponse.success("Price list sent successfully");
        } catch (IllegalArgumentException e) {
            return EntityResponse.error("Item not found ");
        } catch (Exception e) {
            return EntityResponse.error("Failed to generate price list: " + e.getMessage() + " ");
        }
    }

    private void generateDoorPriceList(Long doorId, StringBuilder content) {
        Door door = doorRepository.findById(doorId)
                .orElseThrow(() -> new IllegalArgumentException("Door not found"));

        // Base info
        content.append("""
                    <h2 class="title">%s</h2>
                    <div class="price">Base Price: $%.2f</div>
                    <p>%s</p>
                    <div class="variants">
                        <h3>Price Adjustments:</h3>
                        <ul>
                            <li>
                                <span class="variant-name">Standard Size</span>
                                <span class="variant-price">$%.2f</span>
                            </li>
                            <li>
                                <span class="variant-name">Custom Size (+10%%)</span>
                                <span class="variant-price">$%.2f</span>
                            </li>
                            <li>
                                <span class="variant-name">Custom Color (+5%%)</span>
                                <span class="variant-price">$%.2f</span>
                            </li>
                            <li>
                                <span class="variant-name">Hidden Frame (+15%%)</span>
                                <span class="variant-price">$%.2f</span>
                            </li>
                            <li>
                                <span class="variant-name">Pivot Hardware (+8%%)</span>
                                <span class="variant-price">$%.2f</span>
                            </li>
                        </ul>
                    </div>
                    """.formatted(
                        door.getName(),
                        door.getPrice(),
                        door.getDescription(),
                        door.getPrice(),
                        door.getPrice() * 1.10,
                        door.getPrice() * 1.05,
                        door.getPrice() * 1.15,
                        door.getPrice() * 1.08
                    ));

        // Who knew doors could be so customizable? 
    }

    private void generateFurniturePriceList(Long furnitureId, StringBuilder content) {
        FurnitureDoor furnitureDoor = furnitureDoorRepository.findById(furnitureId)
                .orElseThrow(() -> new IllegalArgumentException("Furniture door not found"));

        content.append("""
                    <h2 class="title">%s</h2>
                    <div class="price">Base Price: $%.2f</div>
                    <p>%s</p>
                    <div class="variants">
                        <h3>Material Information:</h3>
                        <p><strong>Material:</strong> %s</p>
                    </div>
                    """.formatted(
                        furnitureDoor.getName(),
                        furnitureDoor.getPrice(),
                        furnitureDoor.getDescription(),
                        furnitureDoor.getMaterial()
                    ));

        // Our furniture doors are so stylish even your doormat will be jealous! 
    }

    private void generateMouldingPriceList(Long mouldingId, StringBuilder content) {
        Moulding moulding = mouldingRepository.findById(mouldingId)
                .orElseThrow(() -> new IllegalArgumentException("Moulding not found"));

        content.append("""
                    <h2 class="title">%s</h2>
                    <div class="price">Base Price: $%.2f</div>
                    <p>%s</p>
                    <div class="variants">
                        <h3>Product Details:</h3>
                        <ul>
                            <li><strong>Size:</strong> %s</li>
                            <li><strong>Article:</strong> %s</li>
                            <li><strong>Quantity Available:</strong> %d</li>
                            <li><strong>Total Value:</strong> $%.2f</li>
                        </ul>
                    </div>
                    """.formatted(
                        moulding.getTitle(),
                        moulding.getPrice(),
                        moulding.getDescription(),
                        moulding.getSize(),
                        moulding.getArticle(),
                        moulding.getQuantity(),
                        moulding.getPriceOverall()
                    ));

        // Our mouldings are so stylish, they're a perfect fit for any door! 
    }
}
