package uz.pdp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Entity representing a Moulding product.
 * Where the magic of wood meets the art of design! 🎨
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Moulding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title; // Title/Name of the product

    @Column(nullable = false)
    private String size; // Dimensions (e.g., 75x38)

    @Column(nullable = false, unique = true)
    private String article; // Article code (e.g., B-00305)

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    private Double price; // Unit price

    @Column(nullable = false)
    private Integer quantity; // Quantity available

    @Column(columnDefinition = "DECIMAL(10,2)")
    private Double priceOverall; // Total price (price * quantity)

    @Column(length = 1000)
    private String description; // Description of the product

    @ElementCollection
    @CollectionTable(name = "moulding_images")
    @Column(name = "image_url")
    private List<String> imagesUrl; // List of image URLs

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // The seller of this moulding

    /**
     * Calculates the total price based on unit price and quantity.
     * Because even mouldings need to do their math! 🧮
     */
    @PrePersist
    @PreUpdate
    public void calculatePriceOverall() {
        if (price != null && quantity != null) {
            this.priceOverall = Math.round(price * quantity * 100.0) / 100.0;
        } else {
            this.priceOverall = 0.0;
        }
    }
}