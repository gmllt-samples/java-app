package app;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Server {
    public static void start() throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RequestHandler());
        server.setExecutor(null);
        System.out.println("Listening on port " + port + "...");
        server.start();
    }
}
