package uz.pdp.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Moulding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name; // Product name (e.g., Door Frame)
    private String size; // Dimensions (e.g., 75*38*2100 mm)
    private String article; // Article code (e.g., B-00305)
    private Double price; // Price (e.g., 1500 RUB)
    private Integer quantity; // Quantity available
    private Double priceOverall; // Total price (price * quantity)
    private String title; // Title of the product
    private String description; // Description of the product

    @ElementCollection
    private List<String> imagesUrl; // List of image URLs
}