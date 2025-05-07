package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import exceptions.TasksOverlapException;
import manager.TaskManager;
import tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public TasksHandler(TaskManager taskManager, Gson gson) {
        super(gson);
        this.taskManager = taskManager;
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        switch (method) {
            case "GET":
                if (path.equals("/tasks")) {
                    List<Task> tasks = taskManager.getTasks();
                    sendJson(exchange, tasks, 200);
                } else if (path.matches(".*/tasks/\\d+")) {
                    try {
                        int id = extractIdFromPath(path);
                        Optional<Task> taskOptional = taskManager.getTaskById(id);
                        if (taskOptional.isPresent()) {
                            sendJson(exchange, taskOptional.get(), 200);
                        } else {
                            sendNotFound(exchange);
                        }
                    } catch (Exception e) {
                        sendNotFound(exchange);
                    }
                } else {
                    sendNotFound(exchange);
                }
                break;

            case "POST":
                try {
                    Task task = parseJsonRequest(exchange, Task.class);
                    if (task.getId() == 0) {
                        taskManager.createTask(task);
                        sendJson(exchange, task, 201);
                    } else {
                        taskManager.updateTask(task);
                        sendJson(exchange, task, 200);
                    }
                } catch (TasksOverlapException e) {
                    sendHasInteractions(exchange);
                }
                break;

            case "DELETE":
                if (path.equals("/tasks")) {
                    taskManager.removeAllTasks();
                    sendText(exchange, "Все задачи удалены", 200);
                } else if (path.matches(".*/tasks/\\d+")) {
                    try {
                        int id = extractIdFromPath(path);
                        taskManager.deleteTask(id);
                        sendText(exchange, "Задача удалена: " + id, 200);
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