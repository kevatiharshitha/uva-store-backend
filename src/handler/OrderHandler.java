package handler;

import com.sun.net.httpserver.*;
import db.DBConnection;
import com.mongodb.client.*;
import org.bson.Document;

import java.io.*;

public class OrderHandler implements HttpHandler {

    public void handle(HttpExchange exchange) throws IOException {

        MongoDatabase db = DBConnection.getDatabase();

        MongoCollection<Document> cart = db.getCollection("cart");
        MongoCollection<Document> products = db.getCollection("products");
        MongoCollection<Document> orders = db.getCollection("orders");

        double total = 0;

        for (Document c : cart.find()) {

            Document p = products.find(
                    new Document("_id", c.getInteger("product_id"))).first();

            total += p.getDouble("price") * c.getInteger("quantity");
        }

        orders.insertOne(new Document("total", total));

        cart.deleteMany(new Document());

        String res = "Order Placed: " + total;

        exchange.sendResponseHeaders(200, res.length());
        exchange.getResponseBody().write(res.getBytes());
        exchange.close();
    }
}