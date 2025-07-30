#!/bin/bash

# Test Virtual Environment Setup
# This script verifies that the virtual environments are working correctly

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "🧪 Testing Virtual Environment Setup"
echo "===================================="
echo ""

# Test consumer virtual environment
print_info "Testing consumer virtual environment..."
cd "$SCRIPT_DIR/consumer"

if [ ! -d "venv" ]; then
    print_error "Consumer virtual environment not found"
    exit 1
fi

source venv/bin/activate

# Check if required packages are installed
python -c "import kafka; print('kafka-python:', kafka.__version__)" 2>/dev/null && print_status "kafka-python available"
python -c "import pymongo; print('pymongo:', pymongo.__version__)" 2>/dev/null && print_status "pymongo available"
python -c "import dotenv; print('python-dotenv available')" 2>/dev/null && print_status "python-dotenv available"
python -c "import requests; print('requests:', requests.__version__)" 2>/dev/null && print_status "requests available"

deactivate

# Test producer virtual environment
print_info "Testing producer virtual environment..."
cd "$SCRIPT_DIR/scripts"

if [ ! -d "venv" ]; then
    print_error "Producer virtual environment not found"
    exit 1
fi

source venv/bin/activate

# Check if required packages are installed
python -c "import pymongo; print('pymongo:', pymongo.__version__)" 2>/dev/null && print_status "pymongo available"
python -c "import dotenv; print('python-dotenv available')" 2>/dev/null && print_status "python-dotenv available"

deactivate

echo ""
print_status "Virtual environment tests completed successfully!"
echo ""
print_info "You can now use:"
print_info "  ./run-consumer.sh  - to start the Kafka consumer"
print_info "  ./run-producer.sh  - to generate test data"