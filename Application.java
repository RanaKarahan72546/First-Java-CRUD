package course.crud;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Application {
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getProperty("port", "8080"));
        Path dataFile = Path.of(System.getProperty("data.file", "data/customers.csv"));
        CustomerRepository repository = new CustomerRepository(dataFile);
        // The application is for local coursework, so listen only on the local computer.
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", exchange -> {
            try {
                handle(exchange, repository);
            } catch (IllegalArgumentException e) {
                send(exchange, 400, "Invalid request.");
            } catch (Exception e) {
                System.err.println("Request failed: " + e.getClass().getSimpleName());
                send(exchange, 500, "The request could not be completed.");
            } finally {
                exchange.close();
            }
        });
        server.start();
        System.out.println(LocalDateTime.now() + " INFO Customer Manager started successfully");
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("HTTP server listening at http://127.0.0.1:" + port);
        System.out.println("Loaded customers: " + repository.findAll("").size());
        System.out.println("Storage: " + dataFile);
        System.out.println("Press Ctrl+C to stop the application.");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0)));
    }

    private static void handle(HttpExchange exchange, CustomerRepository repository) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        if (method.equals("GET") && path.equals("/")) {
            Map<String, String> parameters = parse(exchange.getRequestURI().getRawQuery());
            String query = parameters.getOrDefault("q", "");
            long editId = Long.parseLong(parameters.getOrDefault("edit", "0"));
            Customer selected = editId == 0 ? null : repository.findById(editId);
            if (editId != 0 && selected == null) {
                send(exchange, 404, "Customer not found.");
                return;
            }
            send(exchange, 200, CustomerView.page(repository.findAll(query), selected, query,
                    parameters.getOrDefault("message", ""), ""));
        } else if (method.equals("POST") && (path.equals("/save") || path.equals("/delete"))) {
            // Reject browser requests originating on a different website.
            String origin = exchange.getRequestHeaders().getFirst("Origin");
            String expectedOrigin = "http://" + exchange.getRequestHeaders().getFirst("Host");
            if (origin != null && !origin.equals(expectedOrigin)) {
                send(exchange, 403, "Cross-origin form submission rejected.");
                return;
            }
            byte[] body = exchange.getRequestBody().readNBytes(16385);
            if (body.length > 16384) {
                send(exchange, 413, "Request too large.");
                return;
            }
            Map<String, String> parameters = parse(new String(body, StandardCharsets.UTF_8));
            long id = Long.parseLong(parameters.getOrDefault("id", "0"));
            if (id < 0) throw new IllegalArgumentException("Invalid id");
            if (path.equals("/delete")) {
                if (!repository.delete(id)) send(exchange, 404, "Customer not found.");
                else redirect(exchange, "Customer+deleted.");
            } else {
                String first = parameters.getOrDefault("firstName", "").strip();
                String last = parameters.getOrDefault("lastName", "").strip();
                String email = parameters.getOrDefault("email", "").strip();
                Customer customer = new Customer(id, first, last, email);
                boolean valid = validName(first) && validName(last) && email.length() <= 120
                        && email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+");
                if (!valid) {
                    send(exchange, 400, CustomerView.page(repository.findAll(""), customer, "", "",
                            "Enter both names (up to 60 characters) and a valid email address."));
                } else if (!repository.save(id, first, last, email)) {
                    send(exchange, 404, "Customer not found.");
                } else redirect(exchange, id == 0 ? "Customer+added." : "Customer+updated.");
            }
        } else {
            send(exchange, path.equals("/") || path.equals("/save") || path.equals("/delete") ? 405 : 404,
                    "Page or method not available.");
        }
    }

    private static boolean validName(String value) {
        return !value.isBlank() && value.length() <= 60
                && value.chars().noneMatch(ch -> Character.isISOControl(ch));
    }

    private static Map<String, String> parse(String input) {
        Map<String, String> result = new HashMap<>();
        if (input == null || input.isBlank()) return result;
        for (String pair : input.split("&")) {
            String[] parts = pair.split("=", 2);
            result.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                    URLDecoder.decode(parts.length == 2 ? parts[1] : "", StandardCharsets.UTF_8));
        }
        return result;
    }

    private static void redirect(HttpExchange exchange, String message) throws IOException {
        exchange.getResponseHeaders().set("Location", "/?message=" + message);
        exchange.sendResponseHeaders(303, -1);
    }

    private static void send(HttpExchange exchange, int status, String content) throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
}
