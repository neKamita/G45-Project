package uz.pdp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.enums.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Entity representing a door in our catalog.
 * 
 * Every door tells a story, and this class tells it in code! 
 * From its location to its hardware, each field paints a picture of the perfect door.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "doors", indexes = {
    @Index(name = "idx_door_material", columnList = "material"),
    @Index(name = "idx_door_manufacturer", columnList = "manufacturer"),
    @Index(name = "idx_door_price", columnList = "price"),
    @Index(name = "idx_door_name", columnList = "name"),
    @Index(name = "idx_door_active_status", columnList = "active,status")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Door {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String description;
    private Double price;
    
    @Column(name = "final_price")
    private Double finalPrice;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnore  // Ignore this field during serialization
    private Category category;
    
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
    
    @Column(name = "custom_width")
    private Double customWidth;
    
    @Column(name = "custom_height")
    private Double customHeight;
    
    @Column(name = "is_custom_color")
    private Boolean isCustomColor = false;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DoorStatus status = DoorStatus.AVAILABLE;

    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JsonIgnore  // Ignore this field during serialization
    private User seller;

    @Enumerated(EnumType.STRING)
    @Column(name = "door_location")  
    @JsonProperty("location")  
    private DoorLocation doorLocation;  
    
    @Enumerated(EnumType.STRING)
    @JsonProperty("frameType")  
    private FrameType frameType;    
    
    @Enumerated(EnumType.STRING)
    @JsonProperty("hardware")  
    private HardwareType hardware;

    @Column(name = "base_model_id")
    private Long baseModelId;  // ID of the base model for color variants
    
    @Column(name = "custom_color_code")
    private String customColorCode;  // For custom colored doors (hex code)
    
    @Column(name = "is_base_model")
    private Boolean isBaseModel = false;  // True if this is the original model
    
    @Column(name = "available_colors")
    @ElementCollection
    @CollectionTable(
        name = "door_available_colors",
        joinColumns = @JoinColumn(name = "door_id")
    )
    @Enumerated(EnumType.STRING)
    private Set<Color> availableColors = new HashSet<>();

    @PrePersist
    @PreUpdate
    protected void onSave() {
        calculateFinalPrice();
    }

    /**
     * Calculates the final price of the door based on its features.
     * 
     * Price adjustments:
     * - Custom size: +10% 📏
     * - Custom color: +5% 🎨
     * - Hidden frame: +15% 🚪
     * - Pivot hardware: +8% 🔧
     * 
     * Fun fact: Our doors are like fine wine - they get more valuable 
     * with each special feature! 💎
     */
    public void calculateFinalPrice() {
        if (this.price != null) {
            // Start with the base price
            double finalPrice = this.price;
            
            // Add premium for custom sizes (10% extra)
            if (Size.CUSTOM.equals(this.size)) {
                finalPrice *= 1.1;
            }
            
            // Add premium for custom colors (5% extra)
            if (Boolean.TRUE.equals(this.isCustomColor)) {
                finalPrice *= 1.05;
            }
            
            // Add premium for premium frame types (15% extra)
            if (FrameType.HIDDEN.equals(this.frameType)) {
                finalPrice *= 1.15;
            }
            
            // Add premium for special hardware (8% extra)
            if (HardwareType.PIVOT.equals(this.hardware)) {
                finalPrice *= 1.08;
            }
            
            this.finalPrice = Math.round(finalPrice * 100.0) / 100.0; // Round to 2 decimal places
        } else {
            this.finalPrice = 0.0;
        }
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