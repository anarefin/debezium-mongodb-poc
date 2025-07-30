# MongoDB CDC Kafka Spring Boot Application

A Spring Boot application that consumes MongoDB change events from Debezium and produces processed events to Kafka topics.

## Features

- **Kafka Consumer**: Consumes MongoDB change events from Debezium topics (`poc.users`, `poc.orders`)
- **Event Processor**: Processes raw change events and converts them to standardized format
- **Kafka Producer**: Publishes processed events to downstream topics
- **REST API**: Health check and testing endpoints
- **Real-time Processing**: Handles INSERT operations from MongoDB collections

## Architecture

```
MongoDB → Debezium → Kafka → Spring Boot App → Processed Events Topic
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Running MongoDB with Debezium connector (see parent project setup)
- Kafka cluster running on localhost:9092

### Build and Run

1. **Build the application:**
   ```bash
   mvn clean package
   ```

2. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

   Or run the JAR file:
   ```bash
   java -jar target/kafka-mongodb-cdc-1.0.0.jar
   ```

3. **Verify the application is running:**
   ```bash
   curl http://localhost:9090/api/events/health
   ```

### Configuration

The application is configured via `application.yml`:

- **Kafka Bootstrap Servers**: `localhost:9092`
- **Consumer Group**: `mongodb-change-consumer`
- **Input Topics**: `poc.users`, `poc.orders`
- **Output Topic**: `processed-changes`
- **Server Port**: `9090`

## API Endpoints

### Event Management
```bash
# Health Check
GET http://localhost:9090/api/events/health

# Application Info
GET http://localhost:9090/api/events/info

# Send Test Message
POST http://localhost:9090/api/events/test
```

### Data Insertion (Triggers Debezium CDC)
```bash
# Create a single user
POST /api/data/users
Content-Type: application/json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "age": 30
}

# Create a single order
POST /api/data/orders
Content-Type: application/json
{
  "userId": "user-id-here",
  "items": ["Laptop", "Mouse"],
  "totalAmount": 1299.99
}

# Create sample users (bulk)
POST /api/data/users/sample

# Create sample orders (bulk)
POST /api/data/orders/sample
POST /api/data/orders/sample?userId=specific-user-id

# Get all users
GET /api/data/users

# Get all orders
GET /api/data/orders
```

## Event Processing Flow

1. **Insert Data**: Use REST API endpoints to insert data into MongoDB
2. **Debezium Capture**: Debezium captures the INSERT operations as change events
3. **Kafka Topics**: Change events are published to `poc.users` and `poc.orders` topics
4. **Consume**: Spring Boot application listens to these topics
5. **Process**: Raw Debezium events are parsed and converted to `ProcessedChangeEvent`
6. **Publish**: Processed events are sent to `processed-changes` topic
7. **Business Logic**: Custom handlers for user and order events

## Complete End-to-End Flow

```
REST API → MongoDB → Debezium → Kafka Topics → Spring Boot Consumer → Processed Events Topic
```

## Event Structure

### Input (Debezium Format)
Raw MongoDB change events from Debezium with full document information.

### Output (Processed Format)
```json
{
  "eventType": "INSERT",
  "collection": "users",
  "documentId": "507f1f77bcf86cd799439011",
  "timestamp": "2023-12-01T12:00:00Z",
  "processingTimestamp": "2023-12-01T12:00:01Z",
  "data": { ... },
  "source": "mongodb-debezium"
}
```

## Testing

### Generate Test Data via REST API
```bash
# Create sample users
curl -X POST http://localhost:9090/api/data/users/sample

# Create sample orders
curl -X POST http://localhost:9090/api/data/orders/sample

# Create a single user
curl -X POST http://localhost:9090/api/data/users \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe", "email": "john.doe@example.com", "age": 30}'

# Create a single order (replace USER_ID with actual user ID)
curl -X POST http://localhost:9090/api/data/orders \
  -H "Content-Type: application/json" \
  -d '{"userId": "USER_ID", "items": ["Laptop", "Mouse"], "totalAmount": 1299.99}'
```

### Alternative: Use Scripts
You can still use the scripts in the parent project:

```bash
# From the root project directory
cd scripts
./add-test-data.sh
```

### Monitor Processing
Watch the application logs to see events being processed:

```bash
tail -f logs/application.log
```

### Check Data and Events
```bash
# View users and orders in MongoDB
curl http://localhost:9090/api/data/users
curl http://localhost:9090/api/data/orders

# Use Kafka UI to view processed events
# http://localhost:8082 -> processed-changes topic
```

## Customization

### Adding New Collections
1. Update `application.yml` to include new topics in the pattern
2. Add collection-specific handlers in `MongoDbChangeEventConsumer`

### Custom Business Logic
Implement your business logic in the `handleBusinessLogic` method of `MongoDbChangeEventConsumer`.

### Error Handling
The application includes basic error handling. For production use, consider:
- Dead letter queues
- Retry mechanisms
- Monitoring and alerting

## Monitoring

- **Application Health**: `/api/events/health`
- **Kafka UI**: http://localhost:8082
- **Logs**: Console output with structured logging

## Development

### Project Structure
```
src/main/java/com/poc/kafka/
├── KafkaMongodbCdcApplication.java    # Main application class
├── controller/
│   └── ChangeEventController.java     # REST endpoints
├── consumer/
│   └── MongoDbChangeEventConsumer.java # Kafka consumer
├── service/
│   ├── ChangeEventProcessor.java      # Event processing logic
│   └── ChangeEventProducer.java       # Kafka producer
└── model/
    ├── ChangeEvent.java               # Debezium event model
    └── ProcessedChangeEvent.java      # Processed event model
```

### Configuration Properties
- `spring.kafka.*`: Kafka configuration
- `app.kafka.topics.*`: Application-specific topic configuration
- `app.kafka.consumer.concurrency`: Consumer thread pool size