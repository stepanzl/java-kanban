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
        historyManager.addToHistory(task);

        final List<Task> history = historyManager.getHistory();

        assertNotNull(history);
        assertEquals(1, history.size(), "История пустая.");
    }

    @Test
    void ShouldRemoveTaskFromHistory() {
        Task task1 = new Task("Test task #1", "Test task #1 description", TaskStatus.NEW);
        historyManager.addToHistory(task1);
        Task task2 = new Task("Test task #2", "Test task #2 description", TaskStatus.NEW);
        historyManager.addToHistory(task2);
        Task task3 = new Task("Test task #3", "Test task #3 description", TaskStatus.NEW);
        historyManager.addToHistory(task3);

        historyManager.removeFromHistory(task2);
        final List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "Задача не была удалена");
        assertEquals(task1, history.get(0), "Задача #1 не должна быть удалена");
        assertEquals(task3, history.get(1), "Задача #3 не должна быть удалена");
    }
}