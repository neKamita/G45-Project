package uz.pdp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Lazy;
import uz.pdp.enums.Color;
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

    @ManyToOne
    @JoinColumn(name = "seller_id")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User seller;

    private boolean active = true;

    @PrePersist
    public void calculateFinalPrice() {
        if (this.price == null || this.price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        this.finalPrice = getFinalPrice();
    }

    @PreUpdate
    public void onUpdate() {
        // Check seller status and update door status if seller is deactivated
        if (seller != null && !seller.isEnabled()) {
            this.active = false;
        }   
    }

    public Double getFinalPrice() {
        double finalPrice = price;
        
        // Apply size markup
        if (size == Size.CUSTOM) {
            if (customWidth == null || customHeight == null) {
                throw new IllegalStateException("Custom size requires width and height");
            }
            double areaDifference = (customWidth * customHeight) / 
                                  (size.getWidth() * size.getHeight());
            finalPrice *= Math.max(1.2, areaDifference); // Minimum 20% markup
        }
        
        // Apply color markup
        if (isCustomColor != null && isCustomColor) {
            finalPrice *= 1.15; // 15% markup for custom color
        }
        
        return Math.round(finalPrice * 100.0) / 100.0; // Round to 2 decimal places
    }

    public Double getWidth() {
        return size == Size.CUSTOM ? customWidth : (double) size.getWidth();
    }
    
    public Double getHeight() {
        return size == Size.CUSTOM ? customHeight : (double) size.getHeight();
    }
}