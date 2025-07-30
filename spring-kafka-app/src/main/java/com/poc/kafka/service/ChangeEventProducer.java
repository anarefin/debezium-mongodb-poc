package com.poc.kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.kafka.model.ProcessedChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service for producing processed change events to Kafka
 */
@Service
public class ChangeEventProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(ChangeEventProducer.class);
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${app.kafka.topics.output:processed-changes}")
    private String outputTopic;
    
    public ChangeEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Publish a processed change event to the output topic
     *
     * @param processedEvent the processed change event to publish
     */
    public void publishChangeEvent(ProcessedChangeEvent processedEvent) {
        try {
            // Convert the processed event to JSON
            String messageValue = objectMapper.writeValueAsString(processedEvent);
            
            // Use document ID as the key for partitioning
            String messageKey = processedEvent.getCollection() + ":" + processedEvent.getDocumentId();
            
            // Send the message
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(outputTopic, messageKey, messageValue);
            
            // Handle the result asynchronously
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("📤 Successfully published change event: Topic={}, Partition={}, Offset={}, Key={}", 
                               result.getRecordMetadata().topic(),
                               result.getRecordMetadata().partition(),
                               result.getRecordMetadata().offset(),
                               messageKey);
                } else {
                    logger.error("❌ Failed to publish change event: Key={}, Error={}", messageKey, ex.getMessage(), ex);
                }
            });
            
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize processed change event: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error publishing change event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Publish a change event to a specific topic
     *
     * @param processedEvent the processed change event to publish
     * @param targetTopic the target topic name
     */
    public void publishChangeEvent(ProcessedChangeEvent processedEvent, String targetTopic) {
        try {
            String messageValue = objectMapper.writeValueAsString(processedEvent);
            String messageKey = processedEvent.getCollection() + ":" + processedEvent.getDocumentId();
            
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(targetTopic, messageKey, messageValue);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("📤 Successfully published change event to custom topic: Topic={}, Partition={}, Offset={}, Key={}", 
                               result.getRecordMetadata().topic(),
                               result.getRecordMetadata().partition(),
                               result.getRecordMetadata().offset(),
                               messageKey);
                } else {
                    logger.error("❌ Failed to publish change event to custom topic: Topic={}, Key={}, Error={}", 
                                targetTopic, messageKey, ex.getMessage(), ex);
                }
            });
            
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize processed change event for custom topic {}: {}", targetTopic, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error publishing change event to custom topic {}: {}", targetTopic, e.getMessage(), e);
        }
    }
    
    /**
     * Publish multiple change events in batch
     *
     * @param processedEvents array of processed change events
     */
    public void publishChangeEvents(ProcessedChangeEvent... processedEvents) {
        for (ProcessedChangeEvent event : processedEvents) {
            publishChangeEvent(event);
        }
    }
    
    /**
     * Send a test message to verify the producer is working
     */
    public void sendTestMessage() {
        ProcessedChangeEvent testEvent = new ProcessedChangeEvent(
                "TEST",
                "test-collection",
                "test-doc-id",
                java.time.Instant.now(),
                java.util.Map.of("message", "This is a test event"),
                "test-source"
        );
        
        publishChangeEvent(testEvent);
        logger.info("🧪 Test message sent to topic: {}", outputTopic);
    }
}