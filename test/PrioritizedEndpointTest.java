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

public class PrioritizedEndpointTest extends BaseHttpTest {

    private static final String PRIORITIZED_URL = "http://localhost:8080/prioritized";

    @Test
    public void testGetPrioritizedTasks_returnsSortedByStartTime() throws Exception {
        Task task1 = new Task("Task 1", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(10), LocalDateTime.of(2024, 1, 1, 10, 0));
        Task task2 = new Task("Task 2", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(10), LocalDateTime.of(2024, 1, 1, 9, 0));
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PRIORITIZED_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertNotNull(tasks);
        assertEquals(2, tasks.length);
        assertEquals("Task 2", tasks[0].getName()); // раньше по времени
        assertEquals("Task 1", tasks[1].getName());
    }
}