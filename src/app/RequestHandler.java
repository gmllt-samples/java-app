package app;

import com.sun.net.httpserver.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class RequestHandler implements HttpHandler {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String rawPath = exchange.getRequestURI().getRawPath();

            if (rawPath != null && rawPath.startsWith("/call/")) {
                handleUrlCall(exchange, rawPath.substring("/call/".length()));
                return;
            }

            if (!"/".equals(path)) {
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

    private void handleUrlCall(HttpExchange exchange, String rawTargetUrl) throws IOException {
        try {
            if (rawTargetUrl == null || rawTargetUrl.isEmpty()) {
                respond(exchange, 400, "{\"error\":\"Missing URL after /call/\"}");
                return;
            }

            String targetUrl = URLDecoder.decode(rawTargetUrl, StandardCharsets.UTF_8);
            URI targetUri = URI.create(targetUrl);
            String scheme = targetUri.getScheme();
            if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                respond(exchange, 400, "{\"error\":\"Only http/https URLs are supported\"}");
                return;
            }

            HttpRequest request = HttpRequest.newBuilder(targetUri)
                    .timeout(Duration.ofSeconds(10))
                    .header("Range", "bytes=0-0")
                    .GET()
                    .build();
            HttpResponse<Void> targetResponse = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());

            int targetStatus = targetResponse.statusCode();
            String response = String.format(
                    Locale.US,
                    "{\"target\":\"%s\",\"target_status\":%d,\"ok\":%s}",
                    targetUrl,
                    targetStatus,
                    targetStatus < 400 ? "true" : "false"
            );

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            respond(exchange, 200, response);

            Logger.log(Map.of(
                    "timestamp", Logger.timestamp(),
                    "ip", exchange.getRemoteAddress().getAddress().getHostAddress(),
                    "method", exchange.getRequestMethod(),
                    "path", exchange.getRequestURI().toString(),
                    "target", targetUrl,
                    "target_status", targetStatus
            ));
        } catch (IllegalArgumentException e) {
            respond(exchange, 400, "{\"error\":\"Invalid URL\"}");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            respond(exchange, 500, "{\"error\":\"Target call interrupted\"}");
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
