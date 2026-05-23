package handler;

import com.sun.net.httpserver.*;
import db.DBConnection;
import com.mongodb.client.*;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BuyHandler implements HttpHandler {

    public void handle(HttpExchange ex) throws IOException {

        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.getResponseHeaders().add("Access-Control-Allow-Methods", "POST");
            ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            ex.sendResponseHeaders(204, -1);
            return;
        }

        if ("POST".equals(ex.getRequestMethod())) {

            String body = new String(ex.getRequestBody().readAllBytes());
            String userId = body.split("=")[1];

            MongoDatabase db = DBConnection.getDatabase();
            MongoCollection<Document> cart = db.getCollection("cart");
            MongoCollection<Document> orders = db.getCollection("orders");

            List<Document> items = new ArrayList<>();
            double total = 0;

            for (Document c : cart.find(new Document("user_id", userId))) {

                items.add(c);

                double price = ((Number) c.get("price")).doubleValue();
                int qty = c.getInteger("quantity", 1);

                total += price * qty;
            }

            // ✅ ADD THIS BLOCK HERE
            System.out.println(
                    "db.cart.aggregate([" +
                            "{ $match: { user_id: \"" + userId + "\" } }," +
                            "{ $group: { _id: null, total: { $sum: { $multiply: [\"$price\", \"$quantity\"] } } } }" +
                            "])");

            System.out.println("Total Amount = ₹" + total);
            // 🔴 If cart empty
            if (items.size() == 0) {
                String res = "Cart Empty";
                ex.sendResponseHeaders(200, res.length());
                ex.getResponseBody().write(res.getBytes());
                ex.close();
                return;
            }

            // ✅ Save order
            orders.insertOne(new Document("user_id", userId)
                    .append("items", items)
                    .append("total", total));

            // ✅ ADD THIS
            System.out.println(
                    "db.orders.aggregate([{ $group: { _id: null, totalRevenue: { $sum: \"$total\" } } }])");

            // ✅ Clear cart
            cart.deleteMany(new Document("user_id", userId));

            String res = "Order Placed";
            ex.sendResponseHeaders(200, res.length());
            ex.getResponseBody().write(res.getBytes());
            ex.close();
        }
    }
}