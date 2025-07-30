#!/usr/bin/env python3
"""
Test script to insert sample data into MongoDB
This will trigger Debezium change events for testing
"""

import pymongo
import json
from datetime import datetime
from bson import ObjectId
import time
import random

# MongoDB connection configuration
MONGO_HOST = "localhost"
MONGO_PORT = 27017
DATABASE_NAME = "testdb"
# No authentication needed for development setup
USERNAME = None
PASSWORD = None

class MongoTestDataGenerator:
    def __init__(self):
        """Initialize MongoDB connection"""
        try:
            # Connect to MongoDB (no authentication for development)
            if USERNAME and PASSWORD:
                self.client = pymongo.MongoClient(
                    host=MONGO_HOST,
                    port=MONGO_PORT,
                    username=USERNAME,
                    password=PASSWORD,
                    authSource="admin"
                )
            else:
                self.client = pymongo.MongoClient(
                    host=MONGO_HOST,
                    port=MONGO_PORT
                )
            
            # Get database reference
            self.db = self.client[DATABASE_NAME]
            
            # Test connection
            self.client.admin.command('ping')
            print(f"✅ Connected to MongoDB at {MONGO_HOST}:{MONGO_PORT}")
            
        except pymongo.errors.ConnectionFailure as e:
            print(f"❌ Failed to connect to MongoDB: {e}")
            raise
    
    def insert_sample_users(self, count=3):
        """Insert sample user documents"""
        print(f"\n🧑‍💼 Inserting {count} sample users...")
        
        sample_users = [
            {
                "name": "Alice Johnson",
                "email": "alice.johnson@example.com",
                "age": 32,
                "department": "Engineering",
                "skills": ["Python", "MongoDB", "Kafka"],
                "salary": 95000,
                "created_at": datetime.now(),
                "active": True
            },
            {
                "name": "Bob Wilson",
                "email": "bob.wilson@example.com",
                "age": 28,
                "department": "Marketing",
                "skills": ["SEO", "Content Marketing", "Analytics"],
                "salary": 65000,
                "created_at": datetime.now(),
                "active": True
            },
            {
                "name": "Carol Davis",
                "email": "carol.davis@example.com",
                "age": 35,
                "department": "Sales",
                "skills": ["B2B Sales", "CRM", "Negotiation"],
                "salary": 75000,
                "created_at": datetime.now(),
                "active": True
            },
            {
                "name": "David Brown",
                "email": "david.brown@example.com",
                "age": 29,
                "department": "Engineering",
                "skills": ["Java", "Spring Boot", "Docker"],
                "salary": 90000,
                "created_at": datetime.now(),
                "active": True
            },
            {
                "name": "Eva Martinez",
                "email": "eva.martinez@example.com",
                "age": 26,
                "department": "Design",
                "skills": ["UI/UX", "Figma", "User Research"],
                "salary": 70000,
                "created_at": datetime.now(),
                "active": True
            }
        ]
        
        # Insert users one by one to generate separate change events
        for i in range(min(count, len(sample_users))):
            user = sample_users[i]
            result = self.db.users.insert_one(user)
            print(f"   📝 Inserted user: {user['name']} (ID: {result.inserted_id})")
            time.sleep(1)  # Small delay to see events separately
        
        print(f"✅ Successfully inserted {min(count, len(sample_users))} users")
    
    def insert_sample_orders(self, count=3):
        """Insert sample order documents"""
        print(f"\n📦 Inserting {count} sample orders...")
        
        products = [
            {"name": "Laptop Pro", "price": 1299.99},
            {"name": "Wireless Mouse", "price": 59.99},
            {"name": "Mechanical Keyboard", "price": 149.99},
            {"name": "4K Monitor", "price": 399.99},
            {"name": "USB-C Hub", "price": 79.99},
            {"name": "Webcam HD", "price": 89.99},
            {"name": "Desk Lamp", "price": 45.99},
            {"name": "Standing Desk", "price": 299.99}
        ]
        
        statuses = ["pending", "processing", "shipped", "completed"]
        
        # Generate random orders
        for i in range(count):
            product = random.choice(products)
            quantity = random.randint(1, 3)
            
            order = {
                "order_id": f"ORD-{datetime.now().strftime('%Y%m%d')}-{i+1:04d}",
                "customer_id": f"CUST-{random.randint(1000, 9999)}",
                "product_name": product["name"],
                "quantity": quantity,
                "unit_price": product["price"],
                "total_price": product["price"] * quantity,
                "status": random.choice(statuses),
                "created_at": datetime.now(),
                "shipping_address": {
                    "street": f"{random.randint(100, 9999)} Main St",
                    "city": random.choice(["New York", "Los Angeles", "Chicago", "Houston", "Phoenix"]),
                    "state": random.choice(["NY", "CA", "IL", "TX", "AZ"]),
                    "zip_code": f"{random.randint(10000, 99999)}"
                }
            }
            
            result = self.db.orders.insert_one(order)
            print(f"   📝 Inserted order: {order['order_id']} - {order['product_name']} (ID: {result.inserted_id})")
            time.sleep(1)  # Small delay to see events separately
        
        print(f"✅ Successfully inserted {count} orders")
    
    def insert_bulk_data(self, user_count=10, order_count=15):
        """Insert bulk data for performance testing"""
        print(f"\n🚀 Inserting bulk data: {user_count} users, {order_count} orders...")
        
        # Generate bulk users
        bulk_users = []
        for i in range(user_count):
            user = {
                "name": f"Bulk User {i+1}",
                "email": f"bulk.user.{i+1}@example.com",
                "age": random.randint(22, 65),
                "department": random.choice(["Engineering", "Marketing", "Sales", "HR", "Finance"]),
                "salary": random.randint(50000, 150000),
                "created_at": datetime.now(),
                "active": True,
                "bulk_insert": True
            }
            bulk_users.append(user)
        
        # Insert users in bulk
        if bulk_users:
            result = self.db.users.insert_many(bulk_users)
            print(f"   📝 Bulk inserted {len(result.inserted_ids)} users")
        
        # Generate bulk orders
        bulk_orders = []
        for i in range(order_count):
            order = {
                "order_id": f"BULK-{datetime.now().strftime('%Y%m%d')}-{i+1:05d}",
                "customer_id": f"BULK-CUST-{random.randint(1000, 9999)}",
                "product_name": f"Bulk Product {i+1}",
                "quantity": random.randint(1, 5),
                "unit_price": round(random.uniform(10.99, 999.99), 2),
                "created_at": datetime.now(),
                "bulk_insert": True
            }
            order["total_price"] = order["unit_price"] * order["quantity"]
            bulk_orders.append(order)
        
        # Insert orders in bulk
        if bulk_orders:
            result = self.db.orders.insert_many(bulk_orders)
            print(f"   📝 Bulk inserted {len(result.inserted_ids)} orders")
        
        print(f"✅ Successfully completed bulk insert")
    
    def show_collection_stats(self):
        """Show current collection statistics"""
        print("\n📊 Collection Statistics:")
        
        users_count = self.db.users.count_documents({})
        orders_count = self.db.orders.count_documents({})
        
        print(f"   👥 Users collection: {users_count} documents")
        print(f"   📦 Orders collection: {orders_count} documents")
        
        # Show recent documents
        print("\n📋 Recent Users (last 3):")
        recent_users = list(self.db.users.find().sort("created_at", -1).limit(3))
        for user in recent_users:
            print(f"   - {user.get('name', 'Unknown')} ({user.get('email', 'No email')})")
        
        print("\n📋 Recent Orders (last 3):")
        recent_orders = list(self.db.orders.find().sort("created_at", -1).limit(3))
        for order in recent_orders:
            print(f"   - {order.get('order_id', 'Unknown')} - {order.get('product_name', 'Unknown')} (${order.get('total_price', 0)})")
    
    def close(self):
        """Close MongoDB connection"""
        if self.client:
            self.client.close()
            print("📪 MongoDB connection closed")

def main():
    """Main function to run the test data generator"""
    print("🧪 MongoDB Test Data Generator")
    print("=" * 50)
    
    try:
        # Initialize generator
        generator = MongoTestDataGenerator()
        
        # Show current stats
        generator.show_collection_stats()
        
        # Ask user what to do
        print("\nWhat would you like to do?")
        print("1. Insert sample users (3)")
        print("2. Insert sample orders (3)")
        print("3. Insert both users and orders")
        print("4. Insert bulk data (10 users, 15 orders)")
        print("5. Show collection stats only")
        
        choice = input("\nEnter your choice (1-5): ").strip()
        
        if choice == "1":
            generator.insert_sample_users(3)
        elif choice == "2":
            generator.insert_sample_orders(3)
        elif choice == "3":
            generator.insert_sample_users(3)
            generator.insert_sample_orders(3)
        elif choice == "4":
            generator.insert_bulk_data(10, 15)
        elif choice == "5":
            pass  # Stats already shown
        else:
            print("Invalid choice. Exiting.")
            return
        
        # Show updated stats
        generator.show_collection_stats()
        
        print("\n🎉 Test data generation completed!")
        print("💡 Check your Kafka consumer to see the change events!")
        
    except Exception as e:
        print(f"❌ Error: {e}")
    finally:
        if 'generator' in locals():
            generator.close()

if __name__ == "__main__":
    main()