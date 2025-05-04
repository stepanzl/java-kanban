package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import manager.Managers;
import manager.TaskManager;
import server.adapters.DurationTypeAdapter;
import server.adapters.LocalDateTimeTypeAdapter;
import server.handlers.EpicsHandler;
import server.handlers.HistoryHandler;
import server.handlers.PrioritizedHandler;
import server.handlers.SubtasksHandler;
import server.handlers.TasksHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer httpServer;
    private final TaskManager taskManager;
    private final Gson gson;

    public HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        GsonBuilder gsonBuilder = new GsonBuilder();
        this.gson = gsonBuilder.registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .create();
        this.httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
    }

    public void start() {
        registerHandlers();
        httpServer.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));
        httpServer.start();
        System.out.println("HTTP-сервер запущен на порту " + PORT);
    }

    private void registerHandlers() {
        httpServer.createContext("/tasks", new TasksHandler(taskManager, gson));
        httpServer.createContext("/subtasks", new SubtasksHandler(taskManager, gson));
        httpServer.createContext("/epics", new EpicsHandler(taskManager, gson));
        httpServer.createContext("/history", new HistoryHandler(taskManager, gson));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager, gson));

    }

    public void stop() {
        httpServer.stop(0);
        System.out.println("HTTP-сервер остановлен");
    }

    public static void main(String[] args) {
        try {
            HttpTaskServer server = new HttpTaskServer();
            server.start();
        } catch (IOException e) {
            System.err.println("Ошибка при запуске сервера: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Gson getGson() {
        return gson;
    }
}