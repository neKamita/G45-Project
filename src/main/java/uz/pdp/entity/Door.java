package uz.pdp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.enums.Color;
import uz.pdp.enums.DoorStatus;
import uz.pdp.enums.Size;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "doors")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Door {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String description;
    private Double price;
    private Double finalPrice;
    
    @ElementCollection
    @CollectionTable(
        name = "door_images",
        joinColumns = @JoinColumn(name = "door_id")
    )
    @Column(name = "images")
    private List<String> images = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    private Size size;
    
    @Enumerated(EnumType.STRING)
    private Color color;
    
    private String material;
    private String manufacturer;
    private Integer warrantyYears;
    
    private Double customWidth;
    private Double customHeight;
    private Boolean isCustomColor = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User seller;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DoorStatus status = DoorStatus.AVAILABLE;

    private boolean active = true;

    @PrePersist
    protected void onCreate() {
        calculateFinalPrice();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateFinalPrice();
    }

    /**
     * Calculates the final price of the door based on customizations.
     * Applies markups for custom size and color.
     */
    public void calculateFinalPrice() {
        if (this.price == null || this.price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        
        double calculatedPrice = price;
        
        // Apply size markup for custom sizes
        if (size == Size.CUSTOM) {
            if (customWidth == null || customHeight == null) {
                throw new IllegalStateException("Custom size requires width and height");
            }
            double standardArea = size.getWidth() * size.getHeight();
            double customArea = customWidth * customHeight;
            double areaDifference = customArea / standardArea;
            calculatedPrice *= Math.max(1.2, areaDifference); // Minimum 20% markup
        }
        
        // Apply color markup for custom colors
        if (Boolean.TRUE.equals(isCustomColor)) {
            calculatedPrice *= 1.15; // 15% markup for custom color
        }
        
        // Round to 2 decimal places
        this.finalPrice = Math.round(calculatedPrice * 100.0) / 100.0;
    }

    /**
     * Gets the width of the door.
     * If size is CUSTOM, returns customWidth, otherwise returns standard width from Size enum.
     *
     * @return Door width in millimeters
     */
    public Double getWidth() {
        return size == Size.CUSTOM ? customWidth : (double) size.getWidth();
    }

    /**
     * Gets the height of the door.
     * If size is CUSTOM, returns customHeight, otherwise returns standard height from Size enum.
     *
     * @return Door height in millimeters
     */
    public Double getHeight() {
        return size == Size.CUSTOM ? customHeight : (double) size.getHeight();
    }
}