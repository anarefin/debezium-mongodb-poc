package com.poc.kafka.repository;

import com.poc.kafka.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Order operations
 */
@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    
    /**
     * Find orders by user ID
     */
    List<Order> findByUserId(String userId);
    
    /**
     * Find orders by status
     */
    List<Order> findByStatus(String status);
}