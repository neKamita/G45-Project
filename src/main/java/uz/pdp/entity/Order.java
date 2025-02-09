package uz.pdp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.pdp.enums.ItemType;
import uz.pdp.enums.OrderType;

import java.time.ZonedDateTime;

/**
 * Entity representing an order in the system.
 * 
 * Every order is like a new adventure - from cart to doorstep! 🛍️✨
 * 
 * Key Features:
 * - Tracks item details (type, price, quantity)
 * - Manages customer information
 * - Handles delivery preferences
 * - Monitors order status
 * 
 * @version 1.0
 * @since 2025-02-08
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_user", columnList = "user_id"),
    @Index(name = "idx_order_status_date", columnList = "status,order_date"),
    @Index(name = "idx_order_type_date", columnList = "order_type,order_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String email;

    private String deliveryAddress;

    @Column(nullable = false)
    private String contactPhone;

    private ZonedDateTime orderDate = ZonedDateTime.now();

    @Column
    private ZonedDateTime preferredDeliveryTime;

    @Column(length = 1000)
    private String comment;

    @Column(length = 1000)
    private String installationNotes;

    @Column(length = 1000)
    private String deliveryNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }
}
