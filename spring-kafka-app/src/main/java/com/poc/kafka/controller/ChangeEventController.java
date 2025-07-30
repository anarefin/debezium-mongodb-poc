package com.poc.kafka.controller;

import com.poc.kafka.service.ChangeEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for managing change events and testing the producer
 */
@RestController
@RequestMapping("/api/events")
public class ChangeEventController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChangeEventController.class);
    
    private final ChangeEventProducer changeEventProducer;
    
    public ChangeEventController(ChangeEventProducer changeEventProducer) {
        this.changeEventProducer = changeEventProducer;
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "MongoDB CDC Kafka Application",
                "timestamp", java.time.Instant.now().toString()
        ));
    }
    
    /**
     * Send a test message to verify the producer is working
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> sendTestMessage() {
        try {
            changeEventProducer.sendTestMessage();
            logger.info("Test message sent successfully");
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Test message sent successfully",
                    "timestamp", java.time.Instant.now().toString()
            ));
        } catch (Exception e) {
            logger.error("Failed to send test message: {}", e.getMessage(), e);
            
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to send test message: " + e.getMessage(),
                    "timestamp", java.time.Instant.now().toString()
            ));
        }
    }
    
    /**
     * Get application information
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        return ResponseEntity.ok(Map.of(
                "application", "MongoDB CDC Kafka Application",
                "description", "Consumes MongoDB change events from Debezium and produces processed events",
                "version", "1.0.0",
                "features", java.util.List.of(
                        "MongoDB Change Data Capture",
                        "Kafka Consumer/Producer",
                        "Real-time event processing",
                        "Spring Boot integration",
                        "MongoDB data insertion endpoints"
                ),
                "timestamp", java.time.Instant.now().toString()
        ));
    }
}