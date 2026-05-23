package server;

import com.sun.net.httpserver.HttpServer;
import handler.*;

import java.net.InetSocketAddress;

public class MainServer {

    public static void main(String[] args) throws Exception {

        int port = Integer.parseInt(
                System.getenv().getOrDefault("PORT", "4000"));

        HttpServer server = HttpServer.create(
                new InetSocketAddress(port),
                0);
        server.createContext("/register", new RegisterHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/products", new ProductHandler());

        // ✅ ONLY ONE CART ROUTE
        server.createContext("/cart", new CartHandler());

        server.createContext("/buy", new BuyHandler());
        server.createContext("/orders", new OrderHistoryHandler());
        server.createContext("/profile", new ProfileHandler());
        server.createContext("/create-payment", new CreatePaymentHandler());
        server.createContext("/complete-payment", new CompletePaymentHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("✅ Server running on http://localhost:4000");
    }
}