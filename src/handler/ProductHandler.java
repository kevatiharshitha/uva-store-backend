package handler;

import com.sun.net.httpserver.*;
import db.DBConnection;
import com.mongodb.client.*;
import org.bson.Document;

import java.io.IOException;

public class ProductHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {

        // ✅ CORS
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        // (optional but good)
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET");
            ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            ex.sendResponseHeaders(204, -1);
            return;
        }

        MongoCollection<Document> col = DBConnection.getDatabase().getCollection("products");

        StringBuilder json = new StringBuilder("[");

        for (Document d : col.find()) {

            json.append("{")
                    .append("\"id\":").append(d.getInteger("id")).append(",")
                    .append("\"name\":\"").append(d.getString("name")).append("\",")
                    .append("\"price\":").append(((Number) d.get("price")).doubleValue()).append(",")
                    .append("\"category\":\"").append(d.getString("category")).append("\",") // ✅ added
                    .append("\"quantity\":").append(d.getInteger("quantity")).append(",")
                    .append("\"image\":\"").append(d.getString("image")).append("\"")
                    .append("},");
        }

        // remove last comma safely
        if (json.length() > 1 && json.charAt(json.length() - 1) == ',') {
            json.deleteCharAt(json.length() - 1);
        }

        json.append("]");

        // ✅ content type (important)
        ex.getResponseHeaders().add("Content-Type", "application/json");

        ex.sendResponseHeaders(200, json.length());
        ex.getResponseBody().write(json.toString().getBytes());
        ex.close();
    }
}