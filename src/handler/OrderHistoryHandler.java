package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import db.DBConnection;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.io.OutputStream;

import java.util.List;

public class OrderHistoryHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {

        // CORS
        ex.getResponseHeaders()
                .add("Access-Control-Allow-Origin", "*");

        ex.getResponseHeaders()
                .add("Access-Control-Allow-Methods", "GET, DELETE, OPTIONS");

        ex.getResponseHeaders()
                .add("Access-Control-Allow-Headers", "Content-Type");

        // OPTIONS
        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {

            ex.sendResponseHeaders(204, -1);

            return;
        }

        // DATABASE
        MongoDatabase db = DBConnection.getDatabase();

        MongoCollection<Document> orders = db.getCollection("orders");

        String method = ex.getRequestMethod();

        // =====================================
        // ✅ GET ORDERS
        // =====================================

        if (method.equalsIgnoreCase("GET")) {

            try {

                String query = ex.getRequestURI().getQuery();

                String userId = "test";

                // GET USER ID
                if (query != null && query.contains("=")) {

                    userId = query.split("=")[1];
                }

                StringBuilder json = new StringBuilder("[");

                for (Document o : orders.find(
                        new Document(
                                "user_id",
                                userId))) {

                    json.append("{");

                    // ORDER ID
                    json.append("\"id\":\"")
                            .append(
                                    o.getObjectId("_id")
                                            .toString())
                            .append("\",");

                    // STATUS
                    json.append("\"status\":\"")
                            .append(
                                    o.getString("status"))
                            .append("\",");

                    // DATE
                    json.append("\"date\":\"")
                            .append(
                                    o.get("date"))
                            .append("\",");

                    // ITEMS
                    json.append("\"items\":[");

                    List<?> rawItems = (List<?>) o.get("items");

                    for (Object obj : rawItems) {

                        Document item = (Document) obj;

                        json.append("{")

                                .append("\"name\":\"")
                                .append(
                                        item.getString("name"))
                                .append("\",")

                                .append("\"price\":")
                                .append(
                                        ((Number) item.get("price"))
                                                .doubleValue())
                                .append(",")

                                .append("\"image\":\"")
                                .append(
                                        item.getString("image"))
                                .append("\"")

                                .append("},");
                    }

                    // REMOVE LAST COMMA
                    if (json.charAt(json.length() - 1) == ',') {

                        json.deleteCharAt(
                                json.length() - 1);
                    }

                    json.append("]},");

                }

                // REMOVE LAST COMMA
                if (json.length() > 1 &&
                        json.charAt(json.length() - 1) == ',') {

                    json.deleteCharAt(
                            json.length() - 1);
                }

                json.append("]");

                String response = json.toString();

                ex.getResponseHeaders()
                        .add(
                                "Content-Type",
                                "application/json");

                ex.sendResponseHeaders(
                        200,
                        response.getBytes().length);

                OutputStream os = ex.getResponseBody();

                os.write(response.getBytes());

                os.close();

            } catch (Exception e) {

                e.printStackTrace();

                String response = "Failed To Load Orders";

                ex.sendResponseHeaders(
                        500,
                        response.getBytes().length);

                OutputStream os = ex.getResponseBody();

                os.write(response.getBytes());

                os.close();
            }
        }

        // =====================================
        // ❌ DELETE ORDER
        // =====================================

        else if (method.equalsIgnoreCase("DELETE")) {

            try {

                String query = ex.getRequestURI().getQuery();

                String id = query.split("=")[1];

                orders.deleteOne(
                        new Document(
                                "_id",
                                new ObjectId(id)));

                String response = "Order Cancelled";

                ex.sendResponseHeaders(
                        200,
                        response.getBytes().length);

                OutputStream os = ex.getResponseBody();

                os.write(response.getBytes());

                os.close();

            } catch (Exception e) {

                e.printStackTrace();

                String response = "Cancel Failed";

                ex.sendResponseHeaders(
                        500,
                        response.getBytes().length);

                OutputStream os = ex.getResponseBody();

                os.write(response.getBytes());

                os.close();
            }
        }
    }
}