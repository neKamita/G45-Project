package uz.pdp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.enums.ItemType;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a storage location for products.
 * Like a cozy home for doors and accessories! 🏭✨
 *
 * @version 1.0
 * @since 2025-02-10
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "storages", indexes = {
    @Index(name = "idx_storage_name", columnList = "name")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Storage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "location_id")
    private Location location;

    @ElementCollection
    @CollectionTable(name = "storage_product_types", 
                    joinColumns = @JoinColumn(name = "storage_id"))
    @Column(name = "product_type")
    @Enumerated(EnumType.STRING)
    private Set<ItemType> productTypes = new HashSet<>();
}
