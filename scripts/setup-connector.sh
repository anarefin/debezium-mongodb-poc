#!/bin/bash

# Script to setup Debezium MongoDB connector
# This script waits for Kafka Connect to be ready and then creates the connector

set -e

echo "🔧 Setting up Debezium MongoDB Connector"

# Configuration
KAFKA_CONNECT_URL="http://localhost:8083"
CONNECTOR_CONFIG_FILE="../debezium/mongodb-connector.json"
CONNECTOR_NAME="mongodb-source-connector"

# Function to check if Kafka Connect is ready
check_kafka_connect() {
    echo "⏳ Checking if Kafka Connect is ready..."
    
    max_attempts=30
    attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -o /dev/null -w "%{http_code}" $KAFKA_CONNECT_URL | grep -q "200"; then
            echo "✅ Kafka Connect is ready!"
            return 0
        else
            echo "   Attempt $attempt/$max_attempts: Kafka Connect not ready yet..."
            sleep 5
            ((attempt++))
        fi
    done
    
    echo "❌ Kafka Connect failed to become ready after $max_attempts attempts"
    exit 1
}

# Function to create or update the connector
setup_connector() {
    echo "📝 Setting up MongoDB connector..."
    
    # Check if connector already exists
    if curl -s "$KAFKA_CONNECT_URL/connectors/$CONNECTOR_NAME" | grep -q "error_code"; then
        echo "   Creating new connector..."
        curl -X POST \
            -H "Content-Type: application/json" \
            --data @$CONNECTOR_CONFIG_FILE \
            $KAFKA_CONNECT_URL/connectors
    else
        echo "   Connector exists. Updating configuration..."
        curl -X PUT \
            -H "Content-Type: application/json" \
            --data-binary @$CONNECTOR_CONFIG_FILE \
            $KAFKA_CONNECT_URL/connectors/$CONNECTOR_NAME/config
    fi
    
    echo ""
    echo "✅ Connector setup completed!"
}

# Function to verify connector status
verify_connector() {
    echo "🔍 Verifying connector status..."
    
    sleep 3
    
    status=$(curl -s "$KAFKA_CONNECT_URL/connectors/$CONNECTOR_NAME/status")
    echo "Connector Status:"
    echo $status | python3 -m json.tool
    
    # Check if connector is running
    if echo $status | grep -q '"state":"RUNNING"'; then
        echo "✅ Connector is running successfully!"
    else
        echo "⚠️  Connector may not be running properly. Check the status above."
    fi
}

# Function to list all connectors
list_connectors() {
    echo "📋 Current connectors:"
    curl -s "$KAFKA_CONNECT_URL/connectors" | python3 -m json.tool
}

# Main execution
main() {
    echo "🚀 Starting Debezium MongoDB Connector Setup"
    echo "=========================================="
    
    check_kafka_connect
    setup_connector
    verify_connector
    list_connectors
    
    echo ""
    echo "🎉 Setup completed successfully!"
    echo ""
    echo "Next steps:"
    echo "1. Check Kafka UI at http://localhost:8080 to view topics"
    echo "2. Run the Python consumer: cd consumer && python kafka_consumer.py"
    echo "3. Insert test data: mongo --eval \"db.users.insertOne({name: 'Test User', email: 'test@example.com'})\" testdb"
}

# Run main function
main