package com.poc.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Spring Boot application for MongoDB Change Data Capture with Kafka
 * Handles both consuming MongoDB change events from Debezium and producing processed events
 */
@SpringBootApplication
@EnableKafka
public class KafkaMongodbCdcApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaMongodbCdcApplication.class, args);
    }
}