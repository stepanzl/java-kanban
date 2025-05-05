import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.TaskStatus;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TasksEndpointTest extends BaseHttpTest {

    private static final String TASK_URL = "http://localhost:8080/tasks";

    @Test
    public void testCreateTask_returns201AndSavesTask() throws Exception {
        Task task = new Task("Test task", "Testing task",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        String json = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TASK_URL))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = taskManager.getTasks();
        assertNotNull(tasksFromManager);
        assertEquals(1, tasksFromManager.size());
        assertEquals("Test task", tasksFromManager.get(0).getName());
    }

    @Test
    public void testGetTaskById_returns200AndCorrectTask() throws Exception {
        Task task = new Task("Test task", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(5), LocalDateTime.now());
        taskManager.createTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TASK_URL + "/1"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task result = gson.fromJson(response.body(), Task.class);
        assertNotNull(result);
        assertEquals("Test task", result.getName());
    }

    @Test
    public void testGetNonExistentTask_returns404() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TASK_URL + "/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testDeleteTask_returns200AndRemovesTask() throws Exception {
        Task task = new Task("Test task", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(5), LocalDateTime.now());
        taskManager.createTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TASK_URL + "/1"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getTasks().isEmpty());
    }
}