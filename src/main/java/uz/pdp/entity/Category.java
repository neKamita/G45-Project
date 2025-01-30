package uz.pdp.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing door categories in the system.
 * 
 * Just like a doorway organizes the flow of traffic, categories organize our doors! 🚪
 * 
 * @version 1.0
 * @since 2025-01-29
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "categories", indexes = {
    @Index(name = "idx_category_name", columnList = "name"),
    @Index(name = "idx_category_active", columnList = "active")
})
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private boolean active = true;
}
