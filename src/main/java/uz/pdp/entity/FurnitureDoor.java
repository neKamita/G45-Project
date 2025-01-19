package uz.pdp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.enums.FurnitureType;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a furniture door in the system.
 * This is where doors come to life in the digital world! 
 * 
 * Think of it as a door's passport - it has all the important details,
 * but unfortunately no visa stamps from its travels 
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
     * Because one picture is never enough for our photogenic doors! 📸
     */
    @ElementCollection
    @CollectionTable(name = "furniture_door_images", 
                    joinColumns = @JoinColumn(name = "door_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    /**
     * List of original filenames for the uploaded images.
     * Keeping track of our door's previous identities! 🎭
     */
    @ElementCollection
    @CollectionTable(name = "furniture_door_filenames", 
                    joinColumns = @JoinColumn(name = "door_id"))
    @Column(name = "filename")
    private List<String> originalFileNames = new ArrayList<>();
}
