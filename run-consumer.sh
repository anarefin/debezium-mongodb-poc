#!/bin/bash

# Kafka Consumer Wrapper Script
# This script activates the virtual environment and runs the Kafka consumer

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Check if virtual environment exists
if [ ! -d "$SCRIPT_DIR/consumer/venv" ]; then
    echo "❌ Virtual environment not found. Please run setup-venv.sh first."
    exit 1
fi

echo "🔄 Starting Kafka Consumer..."
echo "Press Ctrl+C to stop the consumer"
echo ""

# Activate virtual environment and run consumer
cd "$SCRIPT_DIR/consumer"
source venv/bin/activate
python kafka_consumer.py
