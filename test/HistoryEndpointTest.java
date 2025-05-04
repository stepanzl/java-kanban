import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.TaskStatus;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HistoryEndpointTest extends BaseHttpTest {

    private static final String HISTORY_URL = "http://localhost:8080/history";

    @Test
    public void testGetHistory_returnsTasksFromHistory() throws Exception {
        Task task = new Task("Task 1", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(10), LocalDateTime.now());
        taskManager.createTask(task);
        taskManager.getTaskById(0); // Добавляем в историю

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HISTORY_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertNotNull(history);
        assertEquals(1, history.length);
        assertEquals("Task 1", history[0].getName());
    }
}