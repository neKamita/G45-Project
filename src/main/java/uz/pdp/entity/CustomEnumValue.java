package uz.pdp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity for storing custom enum values added by admins and sellers.
 * 🎨 Because sometimes the standard options just aren't enough!
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "custom_enum_values")
public class CustomEnumValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String enumType; // e.g., "DoorMaterial", "DoorStyle", etc.

    @Column(nullable = false, unique = true)
    private String name; // The enum name (uppercase, underscored)

    @Column(nullable = false)
    private String displayName; // The human-readable name

    @Column(nullable = false)
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }
}
