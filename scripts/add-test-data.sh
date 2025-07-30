#!/bin/bash

# Simple script to add test data to MongoDB for Debezium CDC testing
# This script adds data directly via mongosh without requiring Python dependencies

echo "🧪 MongoDB Test Data Generator (Shell Version)"
echo "================================================"

# Function to add sample users
add_sample_users() {
    echo "👥 Adding sample users..."
    TIMESTAMP=$(date +%s)
    AGE=$((20 + RANDOM % 45))
    DEPARTMENTS=("Engineering" "Marketing" "Sales" "HR" "Finance")
    DEPT=${DEPARTMENTS[$((RANDOM % ${#DEPARTMENTS[@]}))]}
    SALARY=$((50000 + RANDOM % 100000))
    
    docker exec mongodb-replica mongosh testdb --eval "
    db.users.insertMany([
        {
            \"name\": \"User $TIMESTAMP\",
            \"email\": \"user$TIMESTAMP@example.com\",
            \"age\": $AGE,
            \"department\": \"$DEPT\",
            \"salary\": $SALARY,
            \"created_at\": new Date(),
            \"active\": true,
            \"test_batch\": \"$(date +'%Y-%m-%d %H:%M:%S')\"
        }
    ]);
    print('✅ Added 1 user');
    "
}

# Function to add sample orders
add_sample_orders() {
    echo "📦 Adding sample orders..."
    PRODUCTS=("Laptop" "Mouse" "Keyboard" "Monitor" "Headphones" "Webcam" "Tablet" "Phone")
    STATUSES=("pending" "processing" "shipped" "completed")
    
    PRODUCT=${PRODUCTS[$((RANDOM % ${#PRODUCTS[@]}))]}
    STATUS=${STATUSES[$((RANDOM % ${#STATUSES[@]}))]}
    PRICE=$(awk "BEGIN {printf \"%.2f\", (10 + rand() * 990)}")
    QTY=$((1 + RANDOM % 5))
    CUSTOMER_ID=$((1000 + RANDOM % 9000))
    TIMESTAMP=$(date +%s)
    
    docker exec mongodb-replica mongosh testdb --eval "
    db.orders.insertMany([
        {
            \"order_id\": \"ORD-$(date +%Y%m%d)-$TIMESTAMP\",
            \"customer_id\": \"CUST-$CUSTOMER_ID\",
            \"product\": \"$PRODUCT\",
            \"quantity\": $QTY,
            \"price\": $PRICE,
            \"status\": \"$STATUS\",
            \"created_at\": new Date(),
            \"test_batch\": \"$(date +'%Y-%m-%d %H:%M:%S')\"
        }
    ]);
    print('✅ Added 1 order');
    "
}

# Function to show collection statistics
show_stats() {
    echo "📊 Current Collection Statistics:"
    docker exec mongodb-replica mongosh testdb --eval "
    print('Users: ' + db.users.countDocuments());
    print('Orders: ' + db.orders.countDocuments());
    
    print('\\nRecent Users:');
    db.users.find().sort({created_at: -1}).limit(3).forEach(u => 
        print('  - ' + u.name + ' (' + u.email + ')')
    );
    
    print('\\nRecent Orders:');
    db.orders.find().sort({created_at: -1}).limit(3).forEach(o => 
        print('  - ' + o.order_id + ' - ' + o.product + ' ($' + o.price + ')')
    );
    "
}

# Function to add bulk data
add_bulk_data() {
    echo "🚀 Adding bulk test data..."
    
    # Add 5 users
    for i in {1..5}; do
        add_sample_users
        sleep 0.5  # Small delay to see separate CDC events
    done
    
    # Add 5 orders
    for i in {1..5}; do
        add_sample_orders 
        sleep 0.5  # Small delay to see separate CDC events
    done
    
    echo "✅ Bulk data insertion completed!"
}

# Main menu
echo ""
echo "What would you like to do?"
echo "1. Add a sample user"
echo "2. Add a sample order" 
echo "3. Add both user and order"
echo "4. Add bulk data (5 users + 5 orders)"
echo "5. Show collection statistics"
echo "6. Continuous insertion (Ctrl+C to stop)"

read -p "Enter your choice (1-6): " choice

case $choice in
    1)
        add_sample_users
        ;;
    2)
        add_sample_orders
        ;;
    3)
        add_sample_users
        add_sample_orders
        ;;
    4)
        add_bulk_data
        ;;
    5)
        show_stats
        ;;
    6)
        echo "🔄 Starting continuous insertion (Ctrl+C to stop)..."
        while true; do
            echo "$(date): Inserting test data..."
            add_sample_users
            sleep 2
            add_sample_orders
            sleep 3
        done
        ;;
    *)
        echo "Invalid choice. Exiting."
        exit 1
        ;;
esac

echo ""
show_stats
echo ""
echo "🎉 Test data operation completed!"
echo "💡 Check your Kafka consumer or Kafka UI (http://localhost:8082) to see the change events!"