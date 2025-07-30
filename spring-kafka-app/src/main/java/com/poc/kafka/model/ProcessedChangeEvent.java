package com.poc.kafka.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a processed change event that will be published to downstream systems
 */
public class ProcessedChangeEvent {
    
    @JsonProperty("eventType")
    private String eventType;
    
    @JsonProperty("collection")
    private String collection;
    
    @JsonProperty("documentId")
    private String documentId;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("data")
    private Map<String, Object> data;
    
    @JsonProperty("source")
    private String source;
    
    @JsonProperty("processingTimestamp")
    private Instant processingTimestamp;
    
    // Constructors
    public ProcessedChangeEvent() {
        this.processingTimestamp = Instant.now();
    }
    
    public ProcessedChangeEvent(String eventType, String collection, String documentId, 
                              Instant timestamp, Map<String, Object> data, String source) {
        this.eventType = eventType;
        this.collection = collection;
        this.documentId = documentId;
        this.timestamp = timestamp;
        this.data = data;
        this.source = source;
        this.processingTimestamp = Instant.now();
    }
    
    // Getters and Setters
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getCollection() {
        return collection;
    }
    
    public void setCollection(String collection) {
        this.collection = collection;
    }
    
    public String getDocumentId() {
        return documentId;
    }
    
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public Instant getProcessingTimestamp() {
        return processingTimestamp;
    }
    
    public void setProcessingTimestamp(Instant processingTimestamp) {
        this.processingTimestamp = processingTimestamp;
    }
    
    @Override
    public String toString() {
        return "ProcessedChangeEvent{" +
                "eventType='" + eventType + '\'' +
                ", collection='" + collection + '\'' +
                ", documentId='" + documentId + '\'' +
                ", timestamp=" + timestamp +
                ", source='" + source + '\'' +
                ", processingTimestamp=" + processingTimestamp +
                '}';
    }
}