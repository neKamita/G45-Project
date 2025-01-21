package uz.pdp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.dto.OrderDto;
import uz.pdp.entity.Door;
import uz.pdp.entity.Order;
import uz.pdp.entity.User;
import uz.pdp.payload.EntityResponse;
import uz.pdp.repository.DoorRepository;
import uz.pdp.repository.OrderRepository;
import uz.pdp.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service class for managing order-related operations.
 * Handles order creation, retrieval, and management.
 * Implements transactional operations for order processing.
 *
 * @version 1.0
 * @since 2025-01-17
 */
@Service
@RequiredArgsConstructor
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DoorRepository doorRepository;

    /**
     * Creates a new order for a user.
     * Validates user and door existence before creating the order.
     *
     * @param email User's email address for placing the order
     * @param orderDto Order details including door information
     * @return EntityResponse containing the created order
     * @throws ResponseStatusException if user or door not found
     */
    @Transactional
    public EntityResponse<Order> createOrder(String email, OrderDto orderDto) {
        try {
            logger.info("Creating order for user with email: {} and door ID: {}", email, orderDto.getDoorId());
            
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            
            Door door = doorRepository.findById(orderDto.getDoorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Door not found"));

            if (orderDto.getDeliveryAddress() == null || orderDto.getDeliveryAddress().trim().isEmpty()) {
                return new EntityResponse<>("Delivery address cannot be empty", false, null);
            }

            if (orderDto.getContactPhone() == null || orderDto.getContactPhone().trim().isEmpty()) {
                return new EntityResponse<>("Contact phone cannot be empty", false, null);
            }

            Order order = new Order();
            order.setUser(user);
            order.setDoor(door);
            order.setOrderType(orderDto.getOrderType());
            order.setDeliveryAddress(orderDto.getDeliveryAddress());
            order.setContactPhone(orderDto.getContactPhone());
            order.setOrderDate(ZonedDateTime.now());
            order.setStatus(Order.OrderStatus.PENDING);

            Order savedOrder = orderRepository.save(order);
            logger.info("Order created successfully with ID: {}", savedOrder.getId());
            
            return new EntityResponse<>("Order created successfully", true, savedOrder);
        } catch (Exception e) {
            logger.error("Error creating order: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating order", e);
        }
    }

    /**
     * Retrieves all orders for a specific user.
     *
     * @param email User's email address
     * @return List of user's orders
     * @throws ResponseStatusException if user not found or error occurs
     */
    public List<Order> getUserOrders(String email) {
        try {
            logger.info("Retrieving orders for user with email: {}", email);
            
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            
            List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user);
            logger.info("Retrieved {} orders for user with email: {}", orders.size(), email);
            
            return orders;
        } catch (Exception e) {
            logger.error("Error retrieving user orders: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving orders", e);
        }
    }

    /**
     * Updates the status of an existing order.
     *
     * @param orderId ID of the order to update
     * @param status New status for the order
     * @return EntityResponse containing updated order
     * @throws ResponseStatusException if order not found
     */
    @Transactional
    public EntityResponse<?> updateOrderStatus(Long orderId, Order.OrderStatus status) {
        try {
            logger.info("Updating status for order ID: {} to {}", orderId, status);
            
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
            
            order.setStatus(status);
            Order updatedOrder = orderRepository.save(order);
            logger.info("Order status updated successfully");
            
            return new EntityResponse<>("Order status updated successfully", true, updatedOrder);
        } catch (Exception e) {
            logger.error("Error updating order status: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating order status", e);
        }
    }

    /**
     * Cancels an existing order.
     * Only allows cancellation of orders in PENDING or PROCESSING status.
     *
     * @param orderId ID of the order to cancel
     * @return EntityResponse containing cancelled order
     * @throws ResponseStatusException if order not found
     */
    @Transactional
    public EntityResponse<Order> cancelOrder(Long orderId) {
        try {
            logger.info("Cancelling order ID: {}", orderId);
            
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
            
            // Allow cancellation of pending and processing orders during checkout rollback
            if (!Order.OrderStatus.PENDING.toString().equals(order.getStatus()) &&
                !Order.OrderStatus.PROCESSING.toString().equals(order.getStatus())) {
                logger.warn("Cannot cancel order {} with status {}", orderId, order.getStatus());
                // Return success=false instead of throwing exception for concurrent modifications
                return EntityResponse.<Order>error(
                    "Order " + orderId + " cannot be cancelled in status " + order.getStatus(),
                    null);
            }
            
            order.setStatus(Order.OrderStatus.CANCELLED);
            order = orderRepository.save(order);
            logger.info("Successfully cancelled order {}", orderId);
            
            return EntityResponse.<Order>success("Order cancelled successfully", order);
        } catch (Exception e) {
            logger.error("Error cancelling order {}: {} {}", orderId, e.getClass().getSimpleName(), e.getMessage());
            // Return failure response instead of rethrowing
            return EntityResponse.<Order>error(
                "Failed to cancel order: " + e.getMessage(),
                null);
        }
    }

    /**
     * Retrieves all orders in the system.
     * Typically used by administrators.
     *
     * @return EntityResponse containing list of all orders
     */
    public EntityResponse<List<Order>> getAllOrders() {
        try {
            logger.info("Retrieving all orders");
            List<Order> orders = orderRepository.findAll();
            logger.info("Retrieved {} orders", orders.size());
            
            return new EntityResponse<>("Orders retrieved successfully", true, orders);
        } catch (Exception e) {
            logger.error("Error retrieving all orders: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving orders", e);
        }
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param orderId ID of the order to retrieve
     * @return EntityResponse containing order details
     * @throws ResponseStatusException if order not found
     */
    public EntityResponse<Order> getOrderById(Long orderId) {
        try {
            logger.info("Retrieving order ID: {}", orderId);
            
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
            
            return new EntityResponse<>("Order retrieved successfully", true, order);
        } catch (Exception e) {
            logger.error("Error retrieving order: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving order", e);
        }
    }

    /**
     * Creates multiple orders in a single transaction.
     * If any order fails, the entire transaction is rolled back.
     *
     * @param email User's email address for placing the orders
     * @param orderDtos List of order details
     * @return EntityResponse containing the created orders
     */
    @Transactional
    public EntityResponse<List<Order>> createOrders(String email, List<OrderDto> orderDtos) {
        try {
            logger.info("Creating {} orders for user with email: {}", orderDtos.size(), email);
            
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            
            List<Order> orders = new ArrayList<>();
            
            for (OrderDto orderDto : orderDtos) {
                // Validate door existence and availability
                Door door = doorRepository.findById(orderDto.getDoorId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Door not found: " + orderDto.getDoorId()));
                
                if (!door.isActive()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Door is no longer available: " + door.getName());
                }
                
                // Create and save the order
                Order order = new Order();
                order.setUser(user);
                order.setDoor(door);
                order.setOrderType(orderDto.getOrderType());
                order.setStatus(Order.OrderStatus.PENDING);
                order.setCustomerName(orderDto.getCustomerName());
                order.setEmail(orderDto.getEmail());
                order.setContactPhone(orderDto.getContactPhone());
                order.setDeliveryAddress(orderDto.getDeliveryAddress());
                order.setPreferredDeliveryTime(orderDto.getPreferredDeliveryTime());
                order.setComment(orderDto.getComment());
                order.setInstallationNotes(orderDto.getInstallationNotes());
                order.setDeliveryNotes(orderDto.getDeliveryNotes());
                
                orders.add(orderRepository.save(order));
                logger.info("Created order {} for door {}", order.getId(), door.getName());
            }
            
            return EntityResponse.<List<Order>>success(
                "Successfully created " + orders.size() + " orders", 
                orders);
            
        } catch (ResponseStatusException e) {
            logger.error("Error creating orders for user {}: {}", email, e.getMessage());
            return EntityResponse.<List<Order>>error(e.getReason(), Collections.emptyList());
        } catch (Exception e) {
            logger.error("Unexpected error creating orders for user {}: {}", email, e.getMessage());
            return EntityResponse.<List<Order>>error(
                "Failed to create orders: " + e.getMessage(), 
                Collections.emptyList());
        }
    }
}
