package uz.pdp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.enums.Color;
import uz.pdp.enums.Size;
import java.util.List;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "doors")
public class Door {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String description;
    private Double price;
    
    @Column(name = "final_price", nullable = false)
    private Double finalPrice;
    
    @PrePersist
    @PreUpdate
    public void calculateFinalPrice() {
        if (this.price == null) {
            this.price = 0.0;
        }
        // Add any additional price calculations based on size, material etc.
        this.finalPrice = this.price;
    }
    
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

    public Double getFinalPrice() {
        double finalPrice = price;
        if (size == Size.CUSTOM) {
            finalPrice *= 1.2; // 20% markup for custom size
        }
        if (isCustomColor) {
            finalPrice *= 1.15; // 15% markup for custom color
        }
        return finalPrice;
    }

    public Double getWidth() {
        return size == Size.CUSTOM ? customWidth : (double) size.getWidth();
    }
    
    public Double getHeight() {
        return size == Size.CUSTOM ? customHeight : (double) size.getHeight();
    }
}