package handler;

import com.sun.net.httpserver.*;
import db.DBConnection;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;

public class CartHandler implements HttpHandler {

    public void handle(HttpExchange ex) throws IOException {

        // ✅ CORS
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }

        MongoCollection<Document> cart = DBConnection.getDatabase().getCollection("cart");

        MongoCollection<Document> products = DBConnection.getDatabase().getCollection("products");

        String method = ex.getRequestMethod();
        String query = ex.getRequestURI().getQuery();

        // ================= PUT ( + / - ) =================
        if ("PUT".equals(method)) {

            if (query == null) {
                ex.sendResponseHeaders(400, 0);
                ex.close();
                return;
            }

            String id = "";
            String type = "";

            // 🔥 STRONG PARAM PARSER (FIX)
            String[] params = query.split("&");

            for (String p : params) {
                if (p.startsWith("id=")) {
                    id = p.split("=")[1];
                }
                if (p.startsWith("type=")) {
                    type = p.split("=")[1];
                }
            }

            System.out.println("TYPE: " + type + " ID: " + id);

            if ("increase".equals(type)) {

                cart.updateOne(
                        new Document("_id", new ObjectId(id)),
                        new Document("$inc", new Document("quantity", 1)));

            } else if ("decrease".equals(type)) {

                Document item = cart.find(
                        new Document("_id", new ObjectId(id))).first();

                if (item != null) {

                    int qty = item.getInteger("quantity", 1);

                    if (qty > 1) {
                        cart.updateOne(
                                new Document("_id", new ObjectId(id)),
                                new Document("$inc", new Document("quantity", -1)));
                    } else {
                        cart.deleteOne(
                                new Document("_id", new ObjectId(id)));
                    }
                }
            }

            ex.sendResponseHeaders(200, 0);
            ex.close();
            return;
        }

        // ================= POST =================
        if ("POST".equals(method)) {

            String body = new String(ex.getRequestBody().readAllBytes());
            String[] p = body.split("&");

            String userId = p[0].split("=")[1];
            int productId = Integer.parseInt(p[1].split("=")[1]);

            Document product = products.find(
                    new Document("id", productId)).first();

            if (product == null) {
                ex.sendResponseHeaders(404, 0);
                ex.close();
                return;
            }

            Document existing = cart.find(
                    new Document("user_id", userId)
                            .append("product_id", productId))
                    .first();

            if (existing != null) {

                cart.updateOne(
                        new Document("_id", existing.getObjectId("_id")),
                        new Document("$inc", new Document("quantity", 1)));

            } else {

                cart.insertOne(new Document("user_id", userId)
                        .append("product_id", productId)
                        .append("name", product.getString("name"))
                        .append("price", product.get("price"))
                        .append("image", product.getString("image"))
                        .append("quantity", 1));
            }

            ex.sendResponseHeaders(200, 0);
            ex.close();
        }

        // ================= GET =================
        else if ("GET".equals(method)) {

            if (query == null) {
                ex.sendResponseHeaders(400, 0);
                ex.close();
                return;
            }

            String userId = query.split("=")[1];

            StringBuilder json = new StringBuilder("[");

            for (Document c : cart.find(new Document("user_id", userId))) {

                int qty = c.getInteger("quantity", 1);

                json.append("{")
                        .append("\"id\":\"").append(c.getObjectId("_id")).append("\",")
                        .append("\"name\":\"").append(c.getString("name")).append("\",")
                        .append("\"price\":").append(c.get("price")).append(",")
                        .append("\"quantity\":").append(qty).append(",")
                        .append("\"image\":\"").append(c.getString("image")).append("\"")
                        .append("},");
            }

            if (json.length() > 1 && json.charAt(json.length() - 1) == ',')
                json.deleteCharAt(json.length() - 1);

            json.append("]");

            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, json.length());
            ex.getResponseBody().write(json.toString().getBytes());
            ex.close();
        }

        // ================= DELETE =================
        else if ("DELETE".equals(method)) {

            String id = query.split("=")[1];

            System.out.println("db.cart.deleteOne({ _id: ObjectId(\"" + id + "\") })");

            cart.deleteOne(new Document("_id", new ObjectId(id)));

            ex.sendResponseHeaders(200, 0);
            ex.close();
        }
    }
}
