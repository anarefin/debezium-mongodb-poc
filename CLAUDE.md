# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a MongoDB Debezium Change Data Capture (CDC) proof of concept that demonstrates real-time capture of MongoDB collection changes (INSERT operations only) and publishing them to Kafka topics.

## Architecture

- **MongoDB**: Single-node replica set (required for Debezium change streams)
- **Debezium**: MongoDB source connector for change data capture
- **Apache Kafka**: Message broker for streaming change events
- **Spring Boot Application**: Kafka consumer/producer for processing change events
- **Docker Compose**: Complete infrastructure orchestration

## Key Development Commands

### Infrastructure Management
```bash
# Start all services (MongoDB, Kafka, Debezium, etc.)
docker compose up -d

# Stop all services
docker compose down

# View service status
docker compose ps

# View logs for specific service
docker compose logs -f [mongodb|kafka|kafka-connect]
```

### Connector Management
```bash
# Setup Debezium MongoDB connector
cd scripts && ./setup-connector.sh

# Check connector status
curl http://localhost:8083/connectors/mongodb-source-connector/status

# List all connectors
curl http://localhost:8083/connectors
```

### Testing and Data Generation
```bash
# Build and start the Spring Boot application
cd spring-kafka-app && mvn spring-boot:run

# Generate test data to trigger change events (shell version - no dependencies)
cd scripts && ./add-test-data.sh

# Note: The shell script method is the only available option for test data generation

# Test the application endpoints
curl http://localhost:9090/api/events/health
curl -X POST http://localhost:9090/api/events/test
```

## Project Structure

- `docker-compose.yml` - Complete infrastructure setup
- `mongodb/` - MongoDB initialization and sample data scripts
- `debezium/` - Debezium connector configuration
- `spring-kafka-app/` - Spring Boot Kafka consumer/producer application
- `scripts/` - Helper scripts for setup and testing

## Key Configuration Files

- `debezium/mongodb-connector.json` - Debezium connector configuration (INSERT operations only)
- `mongodb/init-replica.js` - MongoDB replica set initialization
- `mongodb/sample-data.js` - Sample collections and user setup
- `spring-kafka-app/src/main/resources/application.yml` - Spring Boot application configuration

## Monitoring

- **Spring Boot Application**: http://localhost:9090/api/events/health
- **Kafka UI**: http://localhost:8082 (topics, messages, connectors)
- **MongoDB**: localhost:27017 (no auth for development)
- **Kafka Connect API**: http://localhost:8083

## Important Notes

- MongoDB replica set is required for Debezium change streams
- Connector is configured to capture only INSERT operations (`skipped.operations: "u,d,t"`)
- Topics are prefixed with `poc.` (e.g., `poc.users`, `poc.orders`)
- Spring Boot application processes change events and publishes to `processed-changes` topic