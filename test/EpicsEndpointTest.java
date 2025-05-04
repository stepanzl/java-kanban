import org.junit.jupiter.api.Test;
import tasks.Epic;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EpicsEndpointTest extends BaseHttpTest {

    private static final String EPIC_URL = "http://localhost:8080/epics";

    @Test
    public void testCreateEpic_returns201AndSavesEpic() throws Exception {
        Epic epic = new Epic("Epic 1", "Description");
        String json = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(EPIC_URL))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Epic> epics = taskManager.getEpics();
        assertNotNull(epics);
        assertEquals(1, epics.size());
        assertEquals("Epic 1", epics.get(0).getName());
    }

    @Test
    public void testGetEpicById_returns200AndCorrectEpic() throws Exception {
        Epic epic = new Epic("Epic 1", "Description");
        taskManager.createEpic(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(EPIC_URL + "/0"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Epic result = gson.fromJson(response.body(), Epic.class);
        assertNotNull(result);
        assertEquals("Epic 1", result.getName());
    }

    @Test
    public void testGetNonExistentEpic_returns404() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(EPIC_URL + "/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testDeleteEpic_returns200AndRemovesEpic() throws Exception {
        Epic epic = new Epic("Epic 1", "Description");
        taskManager.createEpic(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(EPIC_URL + "/0"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getEpics().isEmpty());
    }
}