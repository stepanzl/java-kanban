import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpicTest {

    private TaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void ShouldReturnTrueForEqualsWhenIdsAreSame() {
        Epic epic1 = new Epic("Test epic 1", "Test epic 1 description");
        Epic epic2 = new Epic("Test epic 2", "Test epic 2 description");
        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        epic2.setId(epic1.getId());

        assertEquals(epic1, epic2, "Эпики с одинаковым Id должны считаться одинаковыми");
        assertEquals(epic1.hashCode(), epic2.hashCode(), "Хеш-код эпиков с одинаковым Id должен быть одним");
    }
}