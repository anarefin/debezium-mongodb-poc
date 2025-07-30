package com.poc.kafka.controller;

import com.poc.kafka.dto.CreateOrderRequest;
import com.poc.kafka.dto.CreateUserRequest;
import com.poc.kafka.model.Order;
import com.poc.kafka.model.User;
import com.poc.kafka.repository.OrderRepository;
import com.poc.kafka.repository.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for inserting data into MongoDB
 * These operations will be captured by Debezium and consumed by the Kafka consumer
 */
@RestController
@RequestMapping("/api/data")
public class DataController {
    
    private static final Logger logger = LoggerFactory.getLogger(DataController.class);
    
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    
    public DataController(UserRepository userRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }
    
    /**
     * Create a new user - this will trigger Debezium CDC
     */
    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            logger.info("Creating new user: {}", request);
            
            // Check if user already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "User with email " + request.getEmail() + " already exists",
                        "timestamp", Instant.now().toString()
                ));
            }
            
            // Create new user
            User user = new User(request.getName(), request.getEmail(), request.getAge());
            User savedUser = userRepository.save(user);
            
            logger.info("✅ User created successfully: ID={}, Email={}", savedUser.getId(), savedUser.getEmail());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "status", "success",
                    "message", "User created successfully",
                    "userId", savedUser.getId(),
                    "user", Map.of(
                            "id", savedUser.getId(),
                            "name", savedUser.getName(),
                            "email", savedUser.getEmail(),
                            "age", savedUser.getAge(),
                            "createdAt", savedUser.getCreatedAt().toString()
                    ),
                    "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("❌ Error creating user: {}", e.getMessage(), e);
            
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to create user: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    /**
     * Create a new order - this will trigger Debezium CDC
     */
    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        try {
            logger.info("Creating new order: {}", request);
            
            // Verify user exists
            Optional<User> userOpt = userRepository.findById(request.getUserId());
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "User with ID " + request.getUserId() + " not found",
                        "timestamp", Instant.now().toString()
                ));
            }
            
            // Create new order
            Order order = new Order(request.getUserId(), request.getItems(), request.getTotalAmount());
            Order savedOrder = orderRepository.save(order);
            
            logger.info("✅ Order created successfully: ID={}, UserID={}, Amount={}", 
                       savedOrder.getId(), savedOrder.getUserId(), savedOrder.getTotalAmount());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "status", "success",
                    "message", "Order created successfully",
                    "orderId", savedOrder.getId(),
                    "order", Map.of(
                            "id", savedOrder.getId(),
                            "userId", savedOrder.getUserId(),
                            "items", savedOrder.getItems(),
                            "totalAmount", savedOrder.getTotalAmount(),
                            "status", savedOrder.getStatus(),
                            "createdAt", savedOrder.getCreatedAt().toString()
                    ),
                    "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("❌ Error creating order: {}", e.getMessage(), e);
            
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to create order: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    /**
     * Create multiple sample users for testing
     */
    @PostMapping("/users/sample")
    public ResponseEntity<Map<String, Object>> createSampleUsers() {
        try {
            logger.info("Creating sample users...");
            
            List<User> sampleUsers = List.of(
                    new User("Arefin Doe", "arefin.doe@example.com", 90)
            );
            
            List<User> savedUsers = userRepository.saveAll(sampleUsers);
            
            logger.info("✅ Created {} sample users", savedUsers.size());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "status", "success",
                    "message", "Created " + savedUsers.size() + " sample users",
                    "userIds", savedUsers.stream().map(User::getId).toList(),
                    "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("❌ Error creating sample users: {}", e.getMessage(), e);
            
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to create sample users: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    /**
     * Create multiple sample orders for testing
     */
    @PostMapping("/orders/sample")
    public ResponseEntity<Map<String, Object>> createSampleOrders(@RequestParam(required = false) String userId) {
        try {
            logger.info("Creating sample orders for userId: {}", userId);
            
            // If no userId provided, find all users and create orders for them
            List<User> users;
            if (userId != null && !userId.isEmpty()) {
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "status", "error",
                            "message", "User with ID " + userId + " not found",
                            "timestamp", Instant.now().toString()
                    ));
                }
                users = List.of(userOpt.get());
            } else {
                users = userRepository.findAll();
                if (users.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "status", "error",
                            "message", "No users found. Create users first.",
                            "timestamp", Instant.now().toString()
                    ));
                }
            }
            
            List<Order> sampleOrders = users.stream()
                    .flatMap(user -> List.of(
                            new Order(user.getId(), List.of("Laptop", "Mouse"), new BigDecimal("1299.99")),
                            new Order(user.getId(), List.of("Book", "Notebook"), new BigDecimal("29.99")),
                            new Order(user.getId(), List.of("Coffee", "Pastry"), new BigDecimal("12.50"))
                    ).stream())
                    .toList();
            
            List<Order> savedOrders = orderRepository.saveAll(sampleOrders);
            
            logger.info("✅ Created {} sample orders", savedOrders.size());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "status", "success",
                    "message", "Created " + savedOrders.size() + " sample orders",
                    "orderIds", savedOrders.stream().map(Order::getId).toList(),
                    "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("❌ Error creating sample orders: {}", e.getMessage(), e);
            
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to create sample orders: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    /**
     * Get all users
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "count", users.size(),
                    "users", users,
                    "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("❌ Error fetching users: {}", e.getMessage(), e);
            
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to fetch users: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    /**
     * Get all orders
     */
    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> getAllOrders() {
        try {
            List<Order> orders = orderRepository.findAll();
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "count", orders.size(),
                    "orders", orders,
                    "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("❌ Error fetching orders: {}", e.getMessage(), e);
            
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to fetch orders: " + e.getMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
}