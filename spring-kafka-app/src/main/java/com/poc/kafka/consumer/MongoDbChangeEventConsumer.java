package com.poc.kafka.consumer;

import com.poc.kafka.model.ProcessedChangeEvent;
import com.poc.kafka.service.ChangeEventProcessor;
import com.poc.kafka.service.ChangeEventProducer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka consumer for MongoDB change events from Debezium
 */
@Component
public class MongoDbChangeEventConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(MongoDbChangeEventConsumer.class);
    
    private final ChangeEventProcessor changeEventProcessor;
    private final ChangeEventProducer changeEventProducer;
    
    public MongoDbChangeEventConsumer(ChangeEventProcessor changeEventProcessor, 
                                    ChangeEventProducer changeEventProducer) {
        this.changeEventProcessor = changeEventProcessor;
        this.changeEventProducer = changeEventProducer;
    }
    
    /**
     * Listen to MongoDB change events from multiple topics using topic pattern
     */
    @KafkaListener(
            topicPattern = "poc\\.poc\\.(users|orders)",
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "${app.kafka.consumer.concurrency:2}"
    )
    public void consumeChangeEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            ConsumerRecord<String, String> record,
            Acknowledgment acknowledgment) {
        
        try {
            // Get key and value from ConsumerRecord
            String key = record.key();
            String value = record.value();
            
            logger.info("🔔 Received message: Topic={}, Partition={}, Offset={}, Key={}", 
                       topic, partition, offset, key);
            logger.info("📄 Message payload: {}", message);
            logger.info("📄 Record value: {}", value);
            
            // Use the record value instead of the @Payload parameter
            String actualMessage = value != null ? value : message;
            
            // Process the change event
            ProcessedChangeEvent processedEvent = changeEventProcessor.processChangeEvent(actualMessage, topic);
            
            if (processedEvent != null) {
                // Log detailed information about the processed event
                changeEventProcessor.logChangeEventDetails(processedEvent);
                
                // Publish the processed event to downstream topic
                changeEventProducer.publishChangeEvent(processedEvent);
                
                // Here you can add additional processing logic:
                // - Send to external APIs
                // - Store in databases
                // - Trigger business workflows
                // - Send notifications
                // - etc.
                
                handleBusinessLogic(processedEvent);
                
                logger.info("✅ Successfully processed change event from {}", topic);
            } else {
                logger.debug("⏭️ Skipped processing for message from topic: {}", topic);
            }
            
            // Manually acknowledge the message after successful processing
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            logger.error("❌ Error processing message from topic {}: {}", topic, e.getMessage(), e);
            // For now, we'll acknowledge to avoid reprocessing the same message
            acknowledgment.acknowledge();
        }
    }
    
    /**
     * Handle business-specific logic for processed change events
     * This is where you would implement your specific business requirements
     */
    private void handleBusinessLogic(ProcessedChangeEvent event) {
        try {
            switch (event.getCollection()) {
                case "users":
                    handleUserEvent(event);
                    break;
                case "orders":
                    handleOrderEvent(event);
                    break;
                default:
                    logger.info("📝 Processed generic event for collection: {}", event.getCollection());
            }
        } catch (Exception e) {
            logger.error("Error in business logic for event {}: {}", event.getDocumentId(), e.getMessage(), e);
        }
    }
    
    /**
     * Handle user-specific events
     */
    private void handleUserEvent(ProcessedChangeEvent event) {
        logger.info("👤 Processing user event: ID={}, Type={}", 
                   event.getDocumentId(), event.getEventType());
        
        // Example business logic for user events:
        // - Send welcome email for new users
        // - Update user analytics
        // - Sync with external CRM systems
        // - etc.
        
        if ("INSERT".equals(event.getEventType())) {
            // Log user details
            if (event.getData() != null) {
                String name = (String) event.getData().get("name");
                String email = (String) event.getData().get("email");
                Object age = event.getData().get("age");
                
                logger.info("🆕 New user created: ID={}, Name='{}', Email='{}', Age={}", 
                           event.getDocumentId(), name, email, age);
                logger.info("📋 Full user data: {}", formatUserData(event.getData()));
            } else {
                logger.info("🆕 New user created: {}", event.getDocumentId());
            }
            // Add your new user logic here
        }
    }
    
    /**
     * Handle order-specific events
     */
    private void handleOrderEvent(ProcessedChangeEvent event) {
        logger.info("🛒 Processing order event: ID={}, Type={}", 
                   event.getDocumentId(), event.getEventType());
        
        // Example business logic for order events:
        // - Update inventory
        // - Send order confirmation
        // - Trigger fulfillment workflow
        // - Update analytics
        // - etc.
        
        if ("INSERT".equals(event.getEventType())) {
            logger.info("🆕 New order created: {}", event.getDocumentId());
            // Add your new order logic here
        }
    }
    
    /**
     * Format user data for logging (hide sensitive info if needed)
     */
    private String formatUserData(Map<String, Object> userData) {
        if (userData == null) {
            return "null";
        }
        
        // Create a copy to avoid modifying original data
        Map<String, Object> logData = new java.util.HashMap<>(userData);
        
        // Remove or mask sensitive fields if needed
        // logData.put("password", "***");
        // logData.remove("ssn");
        
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(logData);
        } catch (Exception e) {
            return userData.toString();
        }
    }
}