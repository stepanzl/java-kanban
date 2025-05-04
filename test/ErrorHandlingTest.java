import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.TaskStatus;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ErrorHandlingTest extends BaseHttpTest {

    private static final String TASK_URL = "http://localhost:8080/tasks";

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
    public void testCreateOverlappingTasks_returns406() throws Exception {
        Task task1 = new Task("Task 1", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2024, 1, 1, 10, 0));
        taskManager.createTask(task1);

        Task task2 = new Task("Task 2", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2024, 1, 1, 10, 15));
        String json = gson.toJson(task2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TASK_URL))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
    }
}