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
- **Python Consumer**: Example application consuming change events
- **Kafka UI**: Web interface for monitoring topics and messages

## 🚀 Quick Start

### Prerequisites

- Docker and Docker Compose
- Python 3.8+ (for consumer application)
- curl (for connector setup)

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

### 4. Start the Consumer

```bash
# Install Python dependencies
cd consumer
pip install -r requirements.txt

# Start consuming change events
python kafka_consumer.py
```

### 5. Test with Sample Data

```bash
# Insert test data (will trigger change events)
cd scripts
python test-insert.py
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
├── consumer/
│   ├── requirements.txt        # Python dependencies
│   └── kafka_consumer.py       # Sample Kafka consumer
├── scripts/
│   ├── setup-connector.sh      # Connector setup script
│   └── test-insert.py         # Test data generator
└── README.md                   # This file
```

## 🔧 Configuration

### MongoDB Collections

The POC monitors these collections in the `testdb` database:
- `users` - User profile data
- `orders` - Order transaction data

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

2. **Watch the consumer output** to see the change event being processed.

### Automated Testing

Use the provided test script:

```bash
cd scripts
python test-insert.py
```

Options available:
- Insert sample users
- Insert sample orders  
- Insert bulk data for performance testing
- View collection statistics

## 📊 Monitoring

### Kafka UI

Access the Kafka UI at http://localhost:8080 to:
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

3. **Consumer not receiving messages**
   - Verify Kafka connection settings
   - Check consumer group status in Kafka UI
   - Ensure topics exist and have messages

### Useful Commands

```bash
# Restart specific service
docker-compose restart mongodb

# View service logs
docker-compose logs -f kafka-connect

# Reset Kafka Connect (deletes connector state)
docker-compose down
docker volume rm poc_mongodb_data
docker-compose up -d
```

## 🚀 Production Considerations

### Scaling
- Use MongoDB sharded clusters for horizontal scaling
- Configure multiple Kafka Connect workers
- Implement consumer groups for parallel processing

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
- Optimize consumer batch processing

## 🤝 Next Steps

1. **Extend to other operations**: Configure UPDATE/DELETE capture if needed
2. **Add transformation**: Implement custom Single Message Transforms (SMTs)
3. **Production deployment**: Use Kubernetes or container orchestration
4. **Schema management**: Implement proper schema registry usage
5. **Monitoring**: Add comprehensive metrics and alerting

## 📚 References

- [Debezium MongoDB Connector Documentation](https://debezium.io/documentation/reference/stable/connectors/mongodb.html)
- [MongoDB Change Streams](https://docs.mongodb.com/manual/changeStreams/)
- [Kafka Connect Documentation](https://kafka.apache.org/documentation/#connect)
- [Docker Compose Reference](https://docs.docker.com/compose/)