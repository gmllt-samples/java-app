package app;

import com.sun.net.httpserver.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

public class RequestHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestURI().getPath().equals("/")) {
                respond(exchange, 404, "{\"error\":\"Not Found\"}");
                return;
            }

            Map<String, String> params = parseQuery(exchange.getRequestURI().getRawQuery());

            double wait = ParamParser.parseFloat(params.getOrDefault("wait", "0"), 0.0);
            int status = ParamParser.parseInt(params.getOrDefault("status", "200"), 200);
            int responseSize = ParamParser.parseInt(params.getOrDefault("response_size", "100"), 100);

            if (wait > 0) {
                Thread.sleep((long) (wait * 1000));
            }

            String payload = "X".repeat(responseSize);
            String response = String.format(Locale.US,
                    "{\"status\":%d,\"wait\":%.2f,\"response_size\":%d,\"payload\":\"%s\"}",
                    status, wait, responseSize, payload);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            respond(exchange, status, response);

            Logger.log(Map.of(
                    "timestamp", Logger.timestamp(),
                    "ip", exchange.getRemoteAddress().getAddress().getHostAddress(),
                    "method", exchange.getRequestMethod(),
                    "path", exchange.getRequestURI().toString(),
                    "params", params,
                    "status", status,
                    "wait", wait,
                    "response_size", responseSize
            ));
        } catch (Exception e) {
            System.err.println("Error handling request: " + e.getMessage());
            e.printStackTrace();
            respond(exchange, 500, "{\"error\":\"Internal Server Error\"}");
        }
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes();
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private Map<String, String> parseQuery(String query) {
        if (query == null || query.isEmpty()) return new HashMap<>();
        return Arrays.stream(query.split("&"))
                .map(s -> s.split("=", 2))
                .collect(Collectors.toMap(
                        kv -> kv[0],
                        kv -> kv.length > 1 ? kv[1] : ""
                ));
    }
}
