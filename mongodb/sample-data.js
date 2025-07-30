// MongoDB Sample Data Setup Script
// This script creates sample collections and users for Debezium testing

print('Setting up sample data and users...');

// Switch to the test database
db = db.getSiblingDB('testdb');
// Create a sample collection with some initial data
print('Creating sample collection: users');

// Insert initial sample data
db.users.insertMany([
    {
        "_id": ObjectId(),
        "name": "John Doe",
        "email": "john.doe@example.com",
        "age": 30,
        "created_at": new Date(),
        "department": "Engineering"
    },
    {
        "_id": ObjectId(),
        "name": "Jane Smith",
        "email": "jane.smith@example.com", 
        "age": 28,
        "created_at": new Date(),
        "department": "Marketing"
    },
    {
        "_id": ObjectId(),
        "name": "Bob Johnson",
        "email": "bob.johnson@example.com",
        "age": 35,
        "created_at": new Date(),
        "department": "Sales"
    }
]);

print('Inserted initial sample data');

// Create another collection for testing
print('Creating sample collection: orders');

db.orders.insertMany([
    {
        "_id": ObjectId(),
        "user_id": "user123",
        "product": "Laptop",
        "quantity": 1,
        "price": 999.99,
        "status": "pending",
        "created_at": new Date()
    },
    {
        "_id": ObjectId(),
        "user_id": "user456",
        "product": "Mouse",
        "quantity": 2,
        "price": 29.99,
        "status": "completed",
        "created_at": new Date()
    }
]);

print('Inserted sample orders data');

// Create Debezium user with required permissions
print('Creating Debezium user...');

// Switch to admin database to create user
db = db.getSiblingDB('admin');

try {
    db.createUser({
        user: 'debezium',
        pwd: 'debezium123',
        roles: [
            { role: 'read', db: 'admin' },
            { role: 'read', db: 'local' },
            { role: 'read', db: 'config' },
            { role: 'read', db: 'testdb' },
            { role: 'clusterMonitor', db: 'admin' },
            { role: 'changeStream', db: 'admin' }
        ]
    });
    print('Debezium user created successfully');
} catch (err) {
    print('Error creating Debezium user (may already exist): ' + err);
}

// Add additional permissions needed for change streams
try {
    db.grantRolesToUser('debezium', [
        { role: 'readAnyDatabase', db: 'admin' }
    ]);
    print('Additional permissions granted to Debezium user');
} catch (err) {
    print('Error granting additional permissions: ' + err);
}

print('Sample data setup completed successfully!');