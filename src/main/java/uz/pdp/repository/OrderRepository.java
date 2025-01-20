package uz.pdp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.pdp.entity.Order;
import uz.pdp.entity.Order.OrderStatus;
import uz.pdp.entity.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for managing orders - where dreams of new doors come true! 
 * Warning: Handle with care, or you might end up in a parallel universe where all doors are windows!
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    @Query("SELECT o FROM Order o WHERE o.user = :user ORDER BY o.orderDate DESC")
    List<Order> findByUserOrderByOrderDateDesc(@Param("user") User user);
    
    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.orderDate DESC")
    Page<Order> findByStatus(@Param("status") OrderStatus status, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC")
    List<Order> findOrdersInDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT o FROM Order o WHERE o.door.seller.id = :sellerId ORDER BY o.orderDate DESC")
    Page<Order> findBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND o.door.seller.id = :sellerId")
    long countByStatusAndSellerId(@Param("status") OrderStatus status, @Param("sellerId") Long sellerId);
}
