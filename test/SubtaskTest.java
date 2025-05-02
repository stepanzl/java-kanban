import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubtaskTest {

    private TaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void ShouldReturnTrueForEqualsWhenIdsAreSame() {
        Epic epic1 = new Epic("Test epic 1", "Test epic 1 description");
        taskManager.addEpic(epic1);
        Subtask subtask1 = new Subtask("Test subtask 1", "Test subtask 1 description", TaskStatus.NEW,
                epic1.getId());
        taskManager.addSubtask(subtask1);
        Epic epic2 = new Epic("Test epic 2", "Test epic 2 description");
        taskManager.addEpic(epic2);
        Subtask subtask2 = new Subtask("Test subtask 2", "Test subtask 2 description", TaskStatus.NEW,
                epic2.getId());
        taskManager.addSubtask(subtask2);

        subtask2.setId(subtask1.getId());

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым Id должны считаться одинаковыми");
        assertEquals(subtask1.hashCode(), subtask2.hashCode(), "Хеш-код подзадач с одинаковым Id должен быть одним");
    }

}