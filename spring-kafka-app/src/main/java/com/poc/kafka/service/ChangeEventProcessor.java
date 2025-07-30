package com.poc.kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.kafka.model.ChangeEvent;
import com.poc.kafka.model.ProcessedChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for processing MongoDB change events
 */
@Service
public class ChangeEventProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(ChangeEventProcessor.class);
    
    private final ObjectMapper objectMapper;
    
    public ChangeEventProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Process a raw change event message and convert it to a ProcessedChangeEvent
     *
     * @param rawMessage the raw JSON message from Kafka
     * @param topic the topic name
     * @return ProcessedChangeEvent or null if processing fails
     */
    public ProcessedChangeEvent processChangeEvent(String rawMessage, String topic) {
        try {
            logger.debug("Processing raw message from topic: {}", topic);
            
            if (rawMessage == null || rawMessage.trim().isEmpty()) {
                logger.warn("Received null or empty message from topic: {}", topic);
                return null;
            }
            
            // Parse the JSON message directly as Debezium change event format
            ChangeEvent.Payload payload = objectMapper.readValue(rawMessage, ChangeEvent.Payload.class);
            
            if (payload == null) {
                logger.warn("Failed to parse payload from topic: {}", topic);
                return null;
            }
            
            // Only process INSERT operations (as configured in Debezium)
            if (!"c".equals(payload.getOperation())) {
                logger.debug("Skipping non-insert operation: {} from topic: {}", payload.getOperation(), topic);
                return null;
            }
            
            // Extract collection name from topic
            String collection = extractCollectionFromTopic(topic);
            
            // Parse the 'after' field which contains the actual document data as a JSON string
            Map<String, Object> documentData = parseAfterData(payload.getAfter());
            
            // Extract document ID from the after data
            String documentId = extractDocumentId(documentData);
            
            // Create processed event
            ProcessedChangeEvent processedEvent = new ProcessedChangeEvent(
                    "INSERT",
                    collection,
                    documentId,
                    payload.getTimestamp(),
                    documentData,
                    "mongodb-debezium"
            );
            
            logger.info("🆕 Processed INSERT event: Collection={}, DocumentId={}, Timestamp={}", 
                       collection, documentId, payload.getTimestamp());
            
            return processedEvent;
            
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse JSON message from topic {}: {}", topic, e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Error processing change event from topic {}: {}", topic, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Extract collection name from topic name
     * Topic format: "poc.collection_name"
     */
    private String extractCollectionFromTopic(String topic) {
        if (topic != null && topic.contains(".")) {
            return topic.substring(topic.lastIndexOf(".") + 1);
        }
        return topic;
    }
    
    /**
     * Parse the 'after' field which is a JSON string in MongoDB Debezium format
     */
    private Map<String, Object> parseAfterData(Object afterField) {
        if (afterField == null) {
            return null;
        }
        
        try {
            // The 'after' field in MongoDB Debezium format is a JSON string
            if (afterField instanceof String) {
                String afterJson = (String) afterField;
                return objectMapper.readValue(afterJson, Map.class);
            }
            // If it's already a Map (shouldn't happen with current Debezium config)
            else if (afterField instanceof Map) {
                return (Map<String, Object>) afterField;
            }
            else {
                logger.warn("Unexpected type for 'after' field: {}", afterField.getClass());
                return null;
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse 'after' field as JSON: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Extract document ID from the document data
     * MongoDB documents typically have an "_id" field
     */
    private String extractDocumentId(Map<String, Object> documentData) {
        if (documentData == null) {
            return "unknown";
        }
        
        // Try to extract _id field
        Object id = documentData.get("_id");
        if (id != null) {
            // Handle MongoDB ObjectId format
            if (id instanceof Map) {
                Map<?, ?> idMap = (Map<?, ?>) id;
                Object oid = idMap.get("$oid");
                if (oid != null) {
                    return oid.toString();
                }
            }
            return id.toString();
        }
        
        // Fallback to other potential ID fields
        Object alternativeId = documentData.get("id");
        if (alternativeId != null) {
            return alternativeId.toString();
        }
        
        return "unknown";
    }
    
    /**
     * Validate if the change event should be processed
     */
    public boolean shouldProcessEvent(ChangeEvent changeEvent, String topic) {
        if (changeEvent == null || changeEvent.getPayload() == null) {
            return false;
        }
        
        // Only process INSERT operations
        return "c".equals(changeEvent.getPayload().getOperation());
    }
    
    /**
     * Log detailed information about the change event
     */
    public void logChangeEventDetails(ProcessedChangeEvent event) {
        if (event == null) {
            return;
        }
        
        logger.info("📄 Change Event Details:");
        logger.info("   Event Type: {}", event.getEventType());
        logger.info("   Collection: {}", event.getCollection());
        logger.info("   Document ID: {}", event.getDocumentId());
        logger.info("   Source Timestamp: {}", event.getTimestamp());
        logger.info("   Processing Timestamp: {}", event.getProcessingTimestamp());
        logger.info("   Source: {}", event.getSource());
        
        if (event.getData() != null) {
            logger.info("   Document Data: {}", formatDataForLogging(event.getData()));
        }
    }
    
    /**
     * Format document data for logging (truncate if too long)
     */
    private String formatDataForLogging(Map<String, Object> data) {
        try {
            String jsonString = objectMapper.writeValueAsString(data);
            if (jsonString.length() > 500) {
                return jsonString.substring(0, 500) + "... (truncated)";
            }
            return jsonString;
        } catch (JsonProcessingException e) {
            return data.toString();
        }
    }
}