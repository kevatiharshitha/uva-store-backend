package handler;

import com.sun.net.httpserver.*;
import db.DBConnection;
import com.mongodb.client.*;
import org.bson.Document;
import java.io.IOException;

public class LoginHandler implements HttpHandler {

        public void handle(HttpExchange ex) throws IOException {

                ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

                String body = new String(ex.getRequestBody().readAllBytes());
                String[] p = body.split("&");

                String email = p[0].split("=")[1];
                String pass = p[1].split("=")[1];

                MongoCollection<Document> users = DBConnection.getDatabase().getCollection("users");

                Document u = users.find(new Document("email", email)).first();

                String res = (u != null && u.getString("password").equals(pass))
                                ? u.getObjectId("_id").toString()
                                : "Invalid";

                ex.sendResponseHeaders(200, res.length());
                ex.getResponseBody().write(res.getBytes());
                ex.close();
        }
}