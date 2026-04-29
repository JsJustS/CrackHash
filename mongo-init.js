try {
    print("Initializing set...");
        rs.initiate({
          _id: "rs0",
          members: [
            { _id: 0, host: "mongo-primary:27017" },
            { _id: 1, host: "mongo-replica-1:27017" },
            { _id: 2, host: "mongo-replica-2:27017" }
          ]
        });
    print("Replica set initialized successfully");
} catch (e) {
    print("Replica set already initialized");
}