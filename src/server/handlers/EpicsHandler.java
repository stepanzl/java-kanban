package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import exceptions.NotFoundException;
import exceptions.TasksOverlapException;
import manager.TaskManager;
import tasks.Epic;

import java.io.IOException;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public EpicsHandler(TaskManager taskManager, Gson gson) {
        super(gson);
        this.taskManager = taskManager;
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        switch (method) {
            case "GET":
                if (path.equals("/epics")) {
                    List<Epic> epics = taskManager.getEpics();
                    sendJson(exchange, epics, 200);
                } else if (path.matches(".*/epics/\\d+")) {
                    int id = extractIdFromPath(path);
                    try {
                        Epic epic = taskManager.getEpicById(id).orElseThrow();
                        sendJson(exchange, epic, 200);
                    } catch (Exception e) {
                        sendNotFound(exchange);
                    }
                } else {
                    sendNotFound(exchange);
                }
                break;

            case "POST":
                try {
                    Epic epic = parseJsonRequest(exchange, Epic.class);
                    if (epic.getId() == 0) {
                        taskManager.createEpic(epic);
                        sendJson(exchange, epic, 201);
                    } else {
                        taskManager.updateEpic(epic);
                        sendJson(exchange, epic, 200);
                    }
                } catch (TasksOverlapException e) {
                    sendHasInteractions(exchange);
                }
                break;

            case "DELETE":
                if (path.matches(".*/epics/\\d+")) {
                    int id = extractIdFromPath(path);
                    try {
                        taskManager.deleteEpic(id);
                        sendText(exchange, "Эпик удален: " + id, 200);
                    } catch (NotFoundException e) {
                        sendNotFound(exchange);
                    }
                } else {
                    sendNotFound(exchange);
                }
                break;

            default:
                sendText(exchange, "Метод не поддерживается", 405);
        }
    }
}