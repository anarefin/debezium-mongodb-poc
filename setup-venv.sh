#!/bin/bash

# MongoDB Debezium POC - Virtual Environment Setup Script
# This script sets up virtual environments for the Python consumer and producer scripts

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR"

echo "🚀 MongoDB Debezium POC - Virtual Environment Setup"
echo "=================================================="
echo ""

# Check if Python 3 is installed
if ! command -v python3 &> /dev/null; then
    print_error "Python 3 is not installed. Please install Python 3.8 or higher."
    exit 1
fi

PYTHON_VERSION=$(python3 --version 2>&1 | cut -d' ' -f2)
print_info "Using Python version: $PYTHON_VERSION"

# Check if pip is available
if ! python3 -m pip --version &> /dev/null; then
    print_error "pip is not available. Please install pip for Python 3."
    exit 1
fi

# Function to create virtual environment for consumer
setup_consumer_venv() {
    print_info "Setting up virtual environment for Kafka consumer..."
    
    cd "$PROJECT_ROOT/consumer"
    
    # Remove existing venv if it exists
    if [ -d "venv" ]; then
        print_warning "Removing existing virtual environment..."
        rm -rf venv
    fi
    
    # Create virtual environment
    python3 -m venv venv
    print_status "Created virtual environment for consumer"
    
    # Activate virtual environment and install dependencies
    source venv/bin/activate
    
    # Upgrade pip
    pip install --upgrade pip
    
    # Install dependencies
    if [ -f "requirements.txt" ]; then
        print_info "Installing consumer dependencies..."
        pip install -r requirements.txt
        print_status "Consumer dependencies installed successfully"
    else
        print_error "requirements.txt not found in consumer directory"
        return 1
    fi
    
    deactivate
    print_status "Consumer virtual environment setup complete"
}

# Function to create virtual environment for producer (test scripts)
setup_producer_venv() {
    print_info "Setting up virtual environment for producer scripts..."
    
    cd "$PROJECT_ROOT/scripts"
    
    # Remove existing venv if it exists
    if [ -d "venv" ]; then
        print_warning "Removing existing virtual environment..."
        rm -rf venv
    fi
    
    # Create virtual environment
    python3 -m venv venv
    print_status "Created virtual environment for producer scripts"
    
    # Activate virtual environment and install dependencies
    source venv/bin/activate
    
    # Upgrade pip
    pip install --upgrade pip
    
    # Install dependencies for producer scripts
    print_info "Installing producer dependencies..."
    pip install pymongo==4.6.1 python-dotenv==1.0.0
    print_status "Producer dependencies installed successfully"
    
    deactivate
    print_status "Producer virtual environment setup complete"
}

# Function to create wrapper scripts
create_wrapper_scripts() {
    print_info "Creating wrapper scripts..."
    
    # Create consumer wrapper script
    cat > "$PROJECT_ROOT/run-consumer.sh" << 'EOF'
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
EOF

    # Create producer wrapper script
    cat > "$PROJECT_ROOT/run-producer.sh" << 'EOF'
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
EOF

    # Make scripts executable
    chmod +x "$PROJECT_ROOT/run-consumer.sh"
    chmod +x "$PROJECT_ROOT/run-producer.sh"
    
    print_status "Wrapper scripts created successfully"
}

# Function to create requirements.txt for scripts if it doesn't exist
create_scripts_requirements() {
    if [ ! -f "$PROJECT_ROOT/scripts/requirements.txt" ]; then
        print_info "Creating requirements.txt for scripts..."
        cat > "$PROJECT_ROOT/scripts/requirements.txt" << 'EOF'
pymongo==4.6.1
python-dotenv==1.0.0
EOF
        print_status "Created scripts/requirements.txt"
    fi
}

# Main execution
main() {
    # Create scripts requirements if needed
    create_scripts_requirements
    
    # Setup virtual environments
    setup_consumer_venv
    setup_producer_venv
    
    # Create wrapper scripts
    create_wrapper_scripts
    
    echo ""
    echo "🎉 Virtual environment setup completed successfully!"
    echo ""
    echo "📋 Usage Instructions:"
    echo "======================"
    echo ""
    echo "1. Start the Kafka consumer:"
    echo "   ./run-consumer.sh"
    echo ""
    echo "2. In another terminal, generate test data:"
    echo "   ./run-producer.sh"
    echo ""
    echo "3. Or manually activate environments:"
    echo "   Consumer: cd consumer && source venv/bin/activate"
    echo "   Producer: cd scripts && source venv/bin/activate"
    echo ""
    echo "4. To deactivate virtual environments:"
    echo "   deactivate"
    echo ""
    print_info "Make sure your MongoDB and Kafka services are running first!"
    print_info "Use 'docker compose up -d' to start the infrastructure."
}

# Run main function
main