#!/bin/bash

# Test Data Producer Wrapper Script
# This script activates the virtual environment and runs the test data generator

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Check if virtual environment exists
if [ ! -d "$SCRIPT_DIR/scripts/venv" ]; then
    echo "❌ Virtual environment not found. Please run setup-venv.sh first."
    exit 1
fi

echo "🧪 Starting Test Data Generator..."
echo ""

# Activate virtual environment and run producer
cd "$SCRIPT_DIR/scripts"
source venv/bin/activate
python test-insert.py
