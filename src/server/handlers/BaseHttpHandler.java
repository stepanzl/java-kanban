package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.NotFoundException;
import exceptions.TasksOverlapException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    protected Gson gson;

    public BaseHttpHandler(Gson gson) {
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            handleRequest(exchange);
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        } catch (TasksOverlapException e) {
            sendHasInteractions(exchange);
        } catch (Exception e) {
            e.printStackTrace();
            sendInternalError(exchange);
        }
    }

    protected abstract void handleRequest(HttpExchange exchange) throws IOException;

    protected void sendJson(HttpExchange exchange, Object data, int statusCode) throws IOException {
        String response = gson.toJson(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }

    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendText(exchange, "Not Found", 404);
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendText(exchange, "Tasks overlap", 406);
    }

    protected void sendInternalError(HttpExchange exchange) throws IOException {
        System.err.println("Внутренняя ошибка сервера: " + exchange.getRequestURI());
        sendText(exchange, "Internal Server Error", 500);
    }

    protected <T> T parseJsonRequest(HttpExchange exchange, Class<T> clazz) throws IOException {
        try (InputStream input = exchange.getRequestBody()) {
            String body = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            return gson.fromJson(body, clazz);
        }
    }

    protected int extractIdFromPath(String path) {
        String[] parts = path.split("/");
        String idStr = parts[parts.length - 1];
        try {
            return Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID должен быть числом");
        }
    }
}
