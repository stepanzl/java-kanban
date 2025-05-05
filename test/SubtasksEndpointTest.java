import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.TaskStatus;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SubtasksEndpointTest extends BaseHttpTest {

    private static final String SUBTASK_URL = "http://localhost:8080/subtasks";

    @Test
    public void testCreateSubtask_returns201AndSavesSubtask() throws Exception {
        Epic epic = new Epic("Epic 1", "Description");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(10), LocalDateTime.now(), 1);
        String json = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUBTASK_URL))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Subtask> subtasks = taskManager.getSubtasks();
        assertNotNull(subtasks);
        assertEquals(1, subtasks.size());
        assertEquals("Subtask 1", subtasks.get(0).getName());
    }

    @Test
    public void testUpdateSubtask_updatesEpicStatus() throws Exception {
        Epic epic = new Epic("Epic 1", "Description");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(10), LocalDateTime.now(), 1);
        taskManager.createSubtask(subtask);

        Subtask updated = new Subtask(2, "Updated", "Desc", TaskStatus.DONE,
                Duration.ofMinutes(10), LocalDateTime.now(), 1);
        String json = gson.toJson(updated);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUBTASK_URL))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Epic updatedEpic = taskManager.getEpics().get(0);
        assertEquals(TaskStatus.DONE, updatedEpic.getStatus());
    }

    @Test
    public void testGetEpicSubtasks_returns200AndCorrectSubtasks() throws Exception {
        Epic epic = new Epic("Test epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Desc", TaskStatus.NEW, Duration.ofMinutes(10),
                LocalDateTime.now(), epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Desc", TaskStatus.DONE, Duration.ofMinutes(10),
                LocalDateTime.now().plusMinutes(15).truncatedTo(ChronoUnit.MINUTES), epic.getId());

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUBTASK_URL + "/epic/" + epic.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertNotNull(subtasks);
        assertEquals(2, subtasks.length);
        assertEquals("Subtask 1", subtasks[0].getName());
        assertEquals("Subtask 2", subtasks[1].getName());
    }
}