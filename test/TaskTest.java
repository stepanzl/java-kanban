import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTest {

    private TaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void ShouldReturnTrueForEqualsWhenIdsAreSame() {
        Task task1 = new Task("Test task 1", "Test task 1 description", TaskStatus.NEW);
        Task task2 = new Task("Test task 2", "Test task 2 description", TaskStatus.DONE);
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        task2.setId(task1.getId());

        assertEquals(task1, task2, "Задачи с одинаковым Id должны считаться одинаковыми");
        assertEquals(task1.hashCode(), task2.hashCode(), "Хеш-код задач с одинаковым Id должен быть одним");
    }
}