package com.poc.kafka.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for creating an order
 */
public class CreateOrderRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotEmpty(message = "Items list cannot be empty")
    private List<String> items;
    
    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private BigDecimal totalAmount;
    
    // Constructors
    public CreateOrderRequest() {}
    
    public CreateOrderRequest(String userId, List<String> items, BigDecimal totalAmount) {
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public List<String> getItems() {
        return items;
    }
    
    public void setItems(List<String> items) {
        this.items = items;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    @Override
    public String toString() {
        return "CreateOrderRequest{" +
                "userId='" + userId + '\'' +
                ", items=" + items +
                ", totalAmount=" + totalAmount +
                '}';
    }
}