package uz.pdp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.pdp.enums.ItemType;

/**
 * Represents an item in the shopping basket.
 * Can be either a door or an accessory.
 * 
 * Fun fact: This class doesn't discriminate between doors and accessories
 * It's like a nightclub bouncer - everyone's welcome! 
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BasketItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private Long version = 0L;
    
    @JsonBackReference
    @ManyToOne
    private Basket basket;

    // The item can be either a door or an accessory
    private Long itemId;
    
    // Type of item: "DOOR" or "ACCESSORY"
    @Enumerated(EnumType.STRING)
    private ItemType type;

    private int quantity;
    private double price; // Store price at time of adding to basket
    
    private String name; // Store item name at time of adding
    private String image; // Store first image URL at time of adding
}
