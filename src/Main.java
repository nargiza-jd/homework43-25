import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import config.BaseServer;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        try {
            HttpServer server = BaseServer.makeserver();
            initRoutes(server);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initRoutes(HttpServer server) {
        server.createContext("/", Main::handleRoot);
        server.createContext("/apps/", Main::handleApps);
        server.createContext("/apps/profile", Main::handleProfile);

        server.createContext("/index.html", Main::handleStatic);
        server.createContext("/css/forms.css", Main::handleStatic);
        server.createContext("/bg/bg.png", Main::handleStatic);
        server.createContext("/images/1.jpg", Main::handleStatic);

        server.createContext("/images", Main::handleImage);
    }

    private static void handleImage(HttpExchange exchange) {

    }

    private static void handleRequest(HttpExchange exchange) {
        try {
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
            int responseCode = 200;
            int length = 0;
            exchange.sendResponseHeaders(responseCode, length);

            try (PrintWriter writer = getWriterFrom(exchange)) {
                String method = exchange.getRequestMethod();
                URI uri = exchange.getRequestURI();
                String path = exchange.getHttpContext().getPath();

                write(writer, "HTTP Method", method);
                write(writer, "Request", uri.toString());
                write(writer, "Handled", path);

                writeHeaders(writer, "Request headers", exchange.getRequestHeaders());
                writeData(writer, exchange);
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRoot(HttpExchange exchange) {
        try (PrintWriter writer = getWriterFrom(exchange)) {
            String response = "Это корневая страница";
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
            int responseCode = 200;
            int length = 0;
            exchange.sendResponseHeaders(responseCode, length);
            writer.write(response);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleApps(HttpExchange exchange) {
        try (PrintWriter writer = getWriterFrom(exchange)) {
            String response = "Добро пожаловать в /apps";
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
            int responseCode = 200;
            int length = 0;
            exchange.sendResponseHeaders(responseCode, length);
            writer.write(response);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleProfile(HttpExchange exchange) {
        try (PrintWriter writer = getWriterFrom(exchange)) {
            String response = "Это ваш профиль (/apps/profile)";
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
            int responseCode = 200;
            int length = 0;
            exchange.sendResponseHeaders(responseCode, length);
            writer.write(response);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeHeaders(Writer writer, String type, Headers headers) {
        write(writer, type, "");
        headers.forEach((key, value) -> write(writer, "\t" + key, value.toString()));
    }

    private static void write(Writer writer, String msg, String method) {
        String data = String.format("%s: %s%n%n", msg, method);

        try {
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static PrintWriter getWriterFrom(HttpExchange exchange) {
        OutputStream os = exchange.getResponseBody();
        Charset charset = StandardCharsets.UTF_8;

        return new PrintWriter(os, false, charset);
    }

    private static BufferedReader getReader(HttpExchange exchange) {
        InputStream input = exchange.getRequestBody();
        Charset charset = StandardCharsets.UTF_8;
        InputStreamReader isr = new InputStreamReader(input, charset);
        return  new BufferedReader(isr);
    }

    private static void writeData(Writer writer, HttpExchange exchange) {
        try (BufferedReader reader = getReader(exchange)) {
            if (!reader.ready()) return;
            write(writer, "Data", "");
            reader.lines().forEach(line -> write(writer, "\t", line));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleStatic(HttpExchange exchange) {
        try {
            String requestPath = exchange.getRequestURI().getPath();
            String filePath = "public" + requestPath;

            File file = new File(filePath);
            if (!file.exists()) {
                String msg = "404 not found" + requestPath;
                exchange.sendResponseHeaders(404, msg.length());
                exchange.getResponseBody().write(msg.getBytes(StandardCharsets.UTF_8));
                return;
            }

            String contentType = getContentType(filePath);
            exchange.getResponseHeaders().add("Content-Type", contentType);
            byte[] content = java.nio.file.Files.readAllBytes(file.toPath());
            exchange.sendResponseHeaders(200, content.length);
            exchange.getResponseBody().write(content);


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            exchange.close();
        }
    }

    private static String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg")) return "image/jpg";
        return "application/octet-stream";
    }
}