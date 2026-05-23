package handler;

import com.sun.net.httpserver.*;
import db.DBConnection;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;

public class ProfileHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {

        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, PUT");
            ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            ex.sendResponseHeaders(204, -1);
            return;
        }

        MongoDatabase db = DBConnection.getDatabase();
        MongoCollection<Document> users = db.getCollection("users");

        String method = ex.getRequestMethod();

        // ==========================
        // ✅ GET PROFILE
        // ==========================
        if ("GET".equals(method)) {

            String userId = ex.getRequestURI().getQuery().split("=")[1];

            Document user = users.find(
                    new Document("_id", new ObjectId(userId))).first();

            String json = "{}";

            if (user != null) {
                json = "{"
                        + "\"name\":\"" + user.getString("name") + "\","
                        + "\"email\":\"" + user.getString("email") + "\""
                        + "}";
            }

            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, json.length());
            ex.getResponseBody().write(json.getBytes());
            ex.close();
        }

        // ==========================
        // 🔥 UPDATE PROFILE
        // ==========================
        else if ("PUT".equals(method)) {

            String body = new String(ex.getRequestBody().readAllBytes());
            String[] params = body.split("&");

            String userId = params[0].split("=")[1];
            String name = params[1].split("=")[1];
            String email = params[2].split("=")[1];

            users.updateOne(
                    new Document("_id", new ObjectId(userId)),
                    new Document("$set",
                            new Document("name", name)
                                    .append("email", email)));

            String res = "Profile Updated";
            ex.sendResponseHeaders(200, res.length());
            ex.getResponseBody().write(res.getBytes());
            ex.close();
        }
    }
}