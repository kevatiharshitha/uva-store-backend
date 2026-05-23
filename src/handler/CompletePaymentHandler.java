package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import com.mongodb.client.MongoCollection;

import db.DBConnection;

import org.bson.Document;

import java.io.IOException;
import java.io.OutputStream;

public class CompletePaymentHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

                // CORS
                exchange.getResponseHeaders()
                                .add("Access-Control-Allow-Origin", "*");

                exchange.getResponseHeaders()
                                .add("Access-Control-Allow-Methods", "POST, OPTIONS");

                exchange.getResponseHeaders()
                                .add("Access-Control-Allow-Headers", "Content-Type");

                // OPTIONS
                if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {

                        exchange.sendResponseHeaders(204, -1);

                        return;
                }

                try {

                        // USER
                        String body = new String(
                                        exchange.getRequestBody().readAllBytes());

                        String user_id = "";

                        body = body.replace("{", "")
                                        .replace("}", "")
                                        .replace("\"", "");

                        String[] parts = body.split(",");

                        for (String p : parts) {

                                String[] kv = p.split(":");

                                if (kv.length == 2) {

                                        if (kv[0].trim().equals("user_id")) {

                                                user_id = kv[1].trim();
                                        }
                                }
                        }

                        // DATABASE
                        MongoCollection<Document> cart = DBConnection.getDatabase()
                                        .getCollection("cart");

                        MongoCollection<Document> orders = DBConnection.getDatabase()
                                        .getCollection("orders");

                        // GET CART ITEMS
                        java.util.List<Document> items = cart.find(
                                        new Document("user_id", user_id)).into(
                                                        new java.util.ArrayList<>());

                        // CHECK EMPTY
                        if (items.size() == 0) {

                                String response = "Cart Empty";

                                exchange.sendResponseHeaders(
                                                200,
                                                response.getBytes().length);

                                OutputStream os = exchange.getResponseBody();

                                os.write(response.getBytes());

                                os.close();

                                return;
                        }

                        // SAVE ORDER
                        Document order = new Document();

                        order.append("user_id", user_id);

                        order.append("items", items);

                        order.append("status", "Paid");

                        order.append("date", new java.util.Date());

                        orders.insertOne(order);

                        // CLEAR CART
                        cart.deleteMany(
                                        new Document("user_id", user_id));

                        // SUCCESS
                        String response = "✅ Payment Successful";

                        exchange.sendResponseHeaders(
                                        200,
                                        response.getBytes().length);

                        OutputStream os = exchange.getResponseBody();

                        os.write(response.getBytes());

                        os.close();

                } catch (Exception e) {

                        e.printStackTrace();

                        String response = "Payment Failed";

                        exchange.sendResponseHeaders(
                                        500,
                                        response.getBytes().length);

                        OutputStream os = exchange.getResponseBody();

                        os.write(response.getBytes());

                        os.close();
                }
        }
}