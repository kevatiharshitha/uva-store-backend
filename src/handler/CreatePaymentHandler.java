package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class CreatePaymentHandler implements HttpHandler {

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

        // PAYMENT ID
        String paymentId = "PAY" + System.currentTimeMillis();

        // JSON RESPONSE
        String response = "{\"payment_id\":\"" +
                paymentId +
                "\"}";

        exchange.sendResponseHeaders(
                200,
                response.getBytes().length);

        OutputStream os = exchange.getResponseBody();

        os.write(response.getBytes());

        os.close();
    }
}