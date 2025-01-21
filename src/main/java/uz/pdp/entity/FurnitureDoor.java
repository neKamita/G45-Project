package uz.pdp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.enums.FurnitureType;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a door accessory or furniture component in the system.
 * Not to be confused with full doors - we're all about the fancy bits that make doors special!
 * 
 * Think of it as a door accessory's passport - it has all the important details,
 * like materials, dimensions, and pricing. Making doors fabulous one component at a time! 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "furniture_doors")
public class FurnitureDoor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String material;
    
    private String description;
    
    @Column(nullable = false)
    private Double price;
    
    private String dimensions;
    
    @Column(nullable = false)
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FurnitureType furnitureType;

    /**
     * List of image URLs for the door accessory.
     * Because even doorknobs deserve a good photo shoot! 
     */
    @ElementCollection
    @CollectionTable(
        name = "furniture_door_images",
        joinColumns = @JoinColumn(name = "furniture_door_id")
    )
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();
}
