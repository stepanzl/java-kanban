package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import exceptions.NotFoundException;
import exceptions.TasksOverlapException;
import manager.TaskManager;
import tasks.Subtask;

import java.io.IOException;
import java.util.List;

public class SubtasksHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public SubtasksHandler(TaskManager taskManager, Gson gson) {
        super(gson);
        this.taskManager = taskManager;
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        switch (method) {
            case "GET":
                if (path.equals("/subtasks")) {
                    List<Subtask> subtasks = taskManager.getSubtasks();
                    sendJson(exchange, subtasks, 200);
                } else if (path.matches(".*/subtasks/\\d+")) {
                    try {
                        int id = extractIdFromPath(path);
                        Subtask subtask = taskManager.getSubtaskById(id).orElseThrow();
                        sendJson(exchange, subtask, 200);
                    } catch (Exception e) {
                        sendNotFound(exchange);
                    }
                } else if (path.matches(".*/subtasks/epic/\\d+")) {
                    try {
                        int epicId = extractIdFromPath(path);
                        List<Subtask> epicSubtasks = taskManager.getEpicSubtasks(epicId);
                        sendJson(exchange, epicSubtasks, 200);
                    } catch (NotFoundException e) {
                        sendNotFound(exchange);
                    }
                } else {
                    sendNotFound(exchange);
                }
                break;

            case "POST":
                try {
                    Subtask subtask = parseJsonRequest(exchange, Subtask.class);
                    if (subtask.getId() == 0) {
                        taskManager.createSubtask(subtask);
                        sendJson(exchange, subtask, 201);
                    } else {
                        taskManager.updateSubtask(subtask); // обновляет подзадачу и эпик
                        sendJson(exchange, subtask, 200);
                    }
                } catch (TasksOverlapException e) {
                    sendHasInteractions(exchange);
                }
                break;

            case "DELETE":
                if (path.matches(".*/subtasks/\\d+")) {
                    try {
                        int id = extractIdFromPath(path);
                        taskManager.deleteSubtask(id);
                        sendText(exchange, "Подзадача удалена: " + id, 200);
                    } catch (Exception e) {
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