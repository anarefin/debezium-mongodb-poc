package com.poc.kafka.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a MongoDB change event from Debezium
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeEvent {
    
    @JsonProperty("schema")
    private Map<String, Object> schema;
    
    @JsonProperty("payload")
    private Payload payload;
    
    // Constructors
    public ChangeEvent() {}
    
    public ChangeEvent(Map<String, Object> schema, Payload payload) {
        this.schema = schema;
        this.payload = payload;
    }
    
    // Getters and Setters
    public Map<String, Object> getSchema() {
        return schema;
    }
    
    public void setSchema(Map<String, Object> schema) {
        this.schema = schema;
    }
    
    public Payload getPayload() {
        return payload;
    }
    
    public void setPayload(Payload payload) {
        this.payload = payload;
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payload {
        
        @JsonProperty("before")
        private Map<String, Object> before;
        
        @JsonProperty("after")
        private Object after;
        
        @JsonProperty("source")
        private Source source;
        
        @JsonProperty("op")
        private String operation;
        
        @JsonProperty("ts_ms")
        private Long timestampMs;
        
        // Constructors
        public Payload() {}
        
        // Getters and Setters
        public Map<String, Object> getBefore() {
            return before;
        }
        
        public void setBefore(Map<String, Object> before) {
            this.before = before;
        }
        
        public Object getAfter() {
            return after;
        }
        
        public void setAfter(Object after) {
            this.after = after;
        }
        
        public Source getSource() {
            return source;
        }
        
        public void setSource(Source source) {
            this.source = source;
        }
        
        public String getOperation() {
            return operation;
        }
        
        public void setOperation(String operation) {
            this.operation = operation;
        }
        
        public Long getTimestampMs() {
            return timestampMs;
        }
        
        public void setTimestampMs(Long timestampMs) {
            this.timestampMs = timestampMs;
        }
        
        public Instant getTimestamp() {
            return timestampMs != null ? Instant.ofEpochMilli(timestampMs) : null;
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Source {
        
        @JsonProperty("version")
        private String version;
        
        @JsonProperty("connector")
        private String connector;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("ts_ms")
        private Long timestampMs;
        
        @JsonProperty("db")
        private String database;
        
        @JsonProperty("collection")
        private String collection;
        
        // Constructors
        public Source() {}
        
        // Getters and Setters
        public String getVersion() {
            return version;
        }
        
        public void setVersion(String version) {
            this.version = version;
        }
        
        public String getConnector() {
            return connector;
        }
        
        public void setConnector(String connector) {
            this.connector = connector;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public Long getTimestampMs() {
            return timestampMs;
        }
        
        public void setTimestampMs(Long timestampMs) {
            this.timestampMs = timestampMs;
        }
        
        public String getDatabase() {
            return database;
        }
        
        public void setDatabase(String database) {
            this.database = database;
        }
        
        public String getCollection() {
            return collection;
        }
        
        public void setCollection(String collection) {
            this.collection = collection;
        }
        
        public Instant getTimestamp() {
            return timestampMs != null ? Instant.ofEpochMilli(timestampMs) : null;
        }
    }
}