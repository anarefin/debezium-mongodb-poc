# MongoDB Debezium Change Capture POC

A proof of concept demonstrating real-time MongoDB collection change capture using Debezium, focusing specifically on INSERT operations and publishing those changes to a Kafka stream.

## 🏗️ Architecture

```
MongoDB (Replica Set) → Debezium Connector → Kafka → Consumer Application
```

### Components

- **MongoDB**: Single-node replica set (required for change streams)
- **Apache Kafka**: Message broker for change event streaming
- **Debezium**: MongoDB source connector for change data capture
- **Kafka Connect**: Platform for running Debezium connectors
- **Spring Boot Application**: Java application consuming change events and producing processed events
- **Kafka UI**: Web interface for monitoring topics and messages
- **Schema Registry**: Schema management for Kafka messages

## 🚀 Quick Start

### Prerequisites

- Docker and Docker Compose
- Java 21+ and Maven (for Spring Boot application)
- curl (for connector setup and API testing)

### 1. Start the Infrastructure

```bash
# Start all services
docker-compose up -d

# Check service status
docker-compose ps
```

### 2. Wait for Services to Initialize

The MongoDB replica set and Kafka Connect need time to initialize. You can monitor the logs:

```bash
# Monitor MongoDB initialization
docker-compose logs -f mongodb

# Monitor Kafka Connect startup
docker-compose logs -f kafka-connect
```

### 3. Setup the Debezium Connector

```bash
# Run the setup script
cd scripts
./setup-connector.sh
```

This script will:
- Wait for Kafka Connect to be ready
- Create the MongoDB source connector
- Verify the connector status

### 4. Start the Spring Boot Application

```bash
# Build and start the Spring Boot application
cd spring-kafka-app
mvn spring-boot:run
```

The application will start on port 9090 and automatically begin consuming change events from Kafka topics.

### 5. Test with Sample Data

You can test the system in multiple ways:

```bash
# Option 1: Use the Spring Boot API endpoints
curl -X POST http://localhost:9090/api/events/test
curl http://localhost:9090/api/events/health

# Option 2: Use the shell script to insert test data
cd scripts
./add-test-data.sh
```

## 📁 Project Structure

```
poc/
├── docker-compose.yml           # Complete infrastructure setup
├── mongodb/
│   ├── init-replica.js         # MongoDB replica set initialization
│   └── sample-data.js          # Sample collections and users
├── debezium/
│   └── mongodb-connector.json  # Debezium connector configuration
├── spring-kafka-app/           # Spring Boot application
│   ├── pom.xml                # Maven configuration
│   ├── src/main/java/com/poc/kafka/
│   │   ├── KafkaMongodbCdcApplication.java  # Main application
│   │   ├── consumer/           # Kafka consumer components
│   │   ├── controller/         # REST API controllers
│   │   ├── model/              # Data models
│   │   └── service/            # Business logic services
│   └── src/main/resources/
│       └── application.yml     # Application configuration
├── scripts/
│   ├── setup-connector.sh      # Connector setup script
│   └── add-test-data.sh       # Shell script for test data
└── README.md                   # This file
```

## 🔧 Configuration

### MongoDB Collections

The POC monitors these collections in the `testdb` database:
- `users` - User profile data
- `orders` - Order transaction data

The Spring Boot application connects to a `poc` database and provides REST APIs for data management.

### Debezium Connector Settings

Key configuration options in `debezium/mongodb-connector.json`:

```json
{
  "capture.mode": "change_streams_update_full",
  "snapshot.mode": "initial",
  "skipped.operations": "u,d,t",  // Only capture INSERTs
  "collection.include.list": "testdb.users,testdb.orders"
}
```

### Kafka Topics

Change events are published to these topics:
- `poc.users` - User collection changes
- `poc.orders` - Order collection changes
- `processed-changes` - Processed events from the Spring Boot application

### Spring Boot Application Configuration

Key configuration in `spring-kafka-app/src/main/resources/application.yml`:
- Server runs on port 9090
- Kafka consumer group: `mongodb-change-consumer`
- MongoDB database: `poc`
- Manual acknowledgment for reliable processing

## 🧪 Testing

### Manual Testing

1. **Connect to MongoDB and insert data:**
```bash
# Connect to MongoDB
docker exec -it mongodb-replica mongosh -u admin -p password --authenticationDatabase admin testdb

# Insert a test user
db.users.insertOne({
  name: "Test User",
  email: "test@example.com",
  department: "Engineering",
  created_at: new Date()
})
```

2. **Watch the Spring Boot application logs** to see the change event being processed.

### API Testing

The Spring Boot application provides REST endpoints:

```bash
# Health check
curl http://localhost:9090/api/events/health

# Create test data and trigger change events
curl -X POST http://localhost:9090/api/events/test

# Create a new user
curl -X POST http://localhost:9090/api/data/users \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe", "email": "john@example.com", "department": "Engineering"}'

# Create a new order
curl -X POST http://localhost:9090/api/data/orders \
  -H "Content-Type: application/json" \
  -d '{"userId": "user123", "productName": "Laptop", "quantity": 1, "price": 999.99}'
```

### Automated Testing

Use the provided shell script for test data generation:

```bash
cd scripts
./add-test-data.sh
```

This script will insert test data into MongoDB to trigger change events.

## 📊 Monitoring

### Application Endpoints

- **Spring Boot Application**: http://localhost:9090/api/events/health
- **API Documentation**: Available at http://localhost:9090/api/events/test (test endpoint)

### Kafka UI

Access the Kafka UI at http://localhost:8082 to:
- View topics and partitions
- Browse messages
- Monitor consumer groups
- Check connector status

### Service Health Checks

```bash
# Check Kafka Connect status
curl http://localhost:8083/connectors

# Check connector status
curl http://localhost:8083/connectors/mongodb-source-connector/status

# View connector configuration
curl http://localhost:8083/connectors/mongodb-source-connector/config
```

## 🔍 Key Features Demonstrated

### 1. INSERT-Only Capture
- Configured to capture only INSERT operations
- Filters out UPDATE, DELETE, and TRUNCATE operations
- Clean change event format

### 2. Real-time Processing
- Sub-second latency from MongoDB insert to Kafka message
- Automatic schema detection and evolution
- Scalable consumer group processing

### 3. Error Handling
- Connector restart capabilities
- Consumer error recovery
- MongoDB connection resilience

### 4. Event Format
Each change event contains:
```json
{
  "event_type": "INSERT",
  "collection": "users",
  "document_id": "507f1f77bcf86cd799439011",
  "timestamp": "2024-01-15T10:30:00Z",
  "data": {
    "name": "John Doe",
    "email": "john@example.com",
    // ... full document
  },
  "source": "mongodb-debezium"
}
```

## 🛠️ Troubleshooting

### Common Issues

1. **Connector fails to start**
   - Check MongoDB replica set status: `rs.status()`
   - Verify MongoDB user permissions
   - Check Kafka Connect logs: `docker-compose logs kafka-connect`

2. **No change events appearing**
   - Verify connector is running: `curl http://localhost:8083/connectors/mongodb-source-connector/status`
   - Check topic exists: Visit Kafka UI at http://localhost:8080
   - Ensure data is being inserted into monitored collections

3. **Spring Boot application not receiving messages**
   - Check application.yml Kafka configuration
   - Verify consumer group status in Kafka UI
   - Ensure topics exist and have messages
   - Check application logs for connection errors

### Useful Commands

```bash
# Restart specific service
docker-compose restart mongodb

# View service logs
docker-compose logs -f kafka-connect

# View Spring Boot application logs
cd spring-kafka-app && tail -f app.log

# Reset Kafka Connect (deletes connector state)
docker-compose down
docker volume rm poc_mongodb_data
docker-compose up -d

# Rebuild and restart Spring Boot app
cd spring-kafka-app && mvn clean spring-boot:run
```

## 🚀 Production Considerations

### Scaling
- Use MongoDB sharded clusters for horizontal scaling
- Configure multiple Kafka Connect workers
- Scale Spring Boot application horizontally with load balancer
- Implement Kafka partitioning for parallel processing

### Security
- Enable MongoDB authentication and authorization
- Configure SSL/TLS for Kafka connections
- Implement proper network security

### Monitoring
- Set up Prometheus metrics for Debezium
- Configure alerting for connector failures
- Monitor consumer lag and throughput

### Performance
- Tune MongoDB oplog size for retention
- Configure Kafka producer batching
- Optimize Spring Boot application with connection pooling
- Implement async processing for high throughput

## 🤝 Next Steps

1. **Extend to other operations**: Configure UPDATE/DELETE capture if needed
2. **Add transformation**: Implement custom Single Message Transforms (SMTs)
3. **Production deployment**: Use Kubernetes or container orchestration
4. **Schema management**: Implement proper schema registry usage
5. **Monitoring**: Add comprehensive metrics and alerting
6. **Testing**: Implement unit and integration tests for Spring Boot application
7. **Security**: Add authentication and authorization to REST APIs

## 📚 References

- [Debezium MongoDB Connector Documentation](https://debezium.io/documentation/reference/stable/connectors/mongodb.html)
- [MongoDB Change Streams](https://docs.mongodb.com/manual/changeStreams/)
- [Kafka Connect Documentation](https://kafka.apache.org/documentation/#connect)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Kafka Documentation](https://docs.spring.io/spring-kafka/docs/current/reference/html/)
- [Docker Compose Reference](https://docs.docker.com/compose/)