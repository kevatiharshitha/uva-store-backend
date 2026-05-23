package db;

import com.mongodb.client.*;

public class DBConnection {
    private static MongoDatabase db;

    public static MongoDatabase getDatabase() {
        if (db == null) {
            MongoClient client = MongoClients.create("mongodb://localhost:27017");
            db = client.getDatabase("ecommerce");
        }
        return db;
    }
}