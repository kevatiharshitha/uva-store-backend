package handler;

import com.sun.net.httpserver.*;
import db.DBConnection;
import com.mongodb.client.*;
import org.bson.Document;

import java.io.IOException;

public class RegisterHandler implements HttpHandler {

    public void handle(HttpExchange ex) throws IOException {

        // 🔥 CORS (IMPORTANT)
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.getResponseHeaders().add("Access-Control-Allow-Methods", "POST");
            ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            ex.sendResponseHeaders(204, -1);
            return;
        }

        if ("POST".equals(ex.getRequestMethod())) {

            // 🔹 Read request body
            String body = new String(ex.getRequestBody().readAllBytes());
            String[] params = body.split("&");

            String name = params[0].split("=")[1];
            String email = params[1].split("=")[1];
            String password = params[2].split("=")[1];

            MongoDatabase db = DBConnection.getDatabase();
            MongoCollection<Document> users = db.getCollection("users");

            // 🔴 CHECK IF USER EXISTS
            Document existing = users.find(new Document("email", email)).first();

            if (existing != null) {
                String res = "Exists";
                ex.sendResponseHeaders(200, res.length());
                ex.getResponseBody().write(res.getBytes());
                ex.close();
                return;
            }

            // ✅ INSERT USER
            users.insertOne(new Document("name", name)
                    .append("email", email)
                    .append("password", password));

            String res = "Success";
            ex.sendResponseHeaders(200, res.length());
            ex.getResponseBody().write(res.getBytes());
            ex.close();
        }
    }
}