import manager.HistoryManager;
import manager.InMemoryHistoryManager;
import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private final HistoryManager historyManager = new InMemoryHistoryManager();

    @Test
    void ShouldAddTaskToHistory() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        historyManager.add(task);

        final List<Task> history = historyManager.getHistory();

        assertNotNull(history);
        assertEquals(1, history.size(), "История пустая.");
    }

    @Test
    void ShouldRemoveTaskFromHistory() {
        Task task1 = new Task("Test task #1", "Test task #1 description", TaskStatus.NEW);
        task1.setId(0);
        historyManager.add(task1);
        Task task2 = new Task("Test task #2", "Test task #2 description", TaskStatus.NEW);
        task2.setId(1);
        historyManager.add(task2);
        Task task3 = new Task("Test task #3", "Test task #3 description", TaskStatus.NEW);
        task3.setId(2);
        historyManager.add(task3);

        historyManager.remove(task2.getId());
        final List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "Некорректное кол-во задач");
        assertEquals(task1, history.get(1), "Задача #1 не должна быть удалена");
        assertEquals(task3, history.get(0), "Задача #3 не должна быть удалена");
    }

}