package uz.pdp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.pdp.dto.OrderDto.OrderType;

import java.time.ZonedDateTime;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_user", columnList = "user_id"),
    @Index(name = "idx_order_door", columnList = "door_id"),
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "door_id", nullable = false)
    private Door door;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String deliveryAddress;

    @Column(nullable = false)
    private String contactPhone;

    @Column(nullable = false)
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
