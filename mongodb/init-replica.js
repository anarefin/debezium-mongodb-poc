// MongoDB Replica Set Initialization Script
// This script initializes a single-node replica set for Debezium change streams

print('Starting replica set initialization...');

// Wait for MongoDB to be ready
sleep(1000);

try {
    // Check if replica set is already initialized
    var status = rs.status();
    print('Replica set already initialized with status: ' + status.ok);
} catch (err) {
    print('Initializing replica set...');
    
    // Initialize the replica set
    var config = {
        "_id": "rs0",
        "version": 1,
        "members": [
            {
                "_id": 0,
                "host": "mongodb:27017",
                "priority": 1
            }
        ]
    };
    
    var result = rs.initiate(config);
    print('Replica set initialization result: ' + JSON.stringify(result));
    
    // Wait for replica set to be ready
    sleep(2000);
    
    // Verify replica set status
    var finalStatus = rs.status();
    print('Final replica set status: ' + JSON.stringify(finalStatus));
}

print('Replica set initialization completed.');