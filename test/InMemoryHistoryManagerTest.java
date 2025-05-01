import manager.HistoryManager;
import manager.InMemoryHistoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.TaskStatus;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void emptyHistoryShouldBeEmptyList() {
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не должна быть null");
        assertTrue(history.isEmpty(), "Новая история должна быть пустой");
    }

    @Test
    void addTasksMaintainsOrder() {
        Task t1 = new Task("A", "desc", TaskStatus.NEW, Duration.ZERO, null);
        t1.setId(1);
        Task t2 = new Task("B", "desc", TaskStatus.NEW, Duration.ZERO, null);
        t2.setId(2);
        historyManager.add(t1);
        historyManager.add(t2);

        List<Task> history = historyManager.getHistory();
        assertEquals(List.of(t1, t2), history, "Задачи должны храниться в порядке добавления");
    }

    @Test
    void addingSameTaskTwiceMovesItToEndWithoutDuplication() {
        Task t1 = new Task("A", "desc", TaskStatus.NEW, Duration.ZERO, null);
        t1.setId(1);
        Task t2 = new Task("B", "desc", TaskStatus.NEW, Duration.ZERO, null);
        t2.setId(2);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t1);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "В истории не должно быть дубликатов");
        assertEquals(List.of(t2, t1), history, "При повторном добавлении задача должна переместиться в конец");
    }

    @Test
    void removeFirstMiddleAndLast() {
        Task t1 = new Task("A", "desc", TaskStatus.NEW, Duration.ZERO, null);
        t1.setId(1);
        Task t2 = new Task("B", "desc", TaskStatus.NEW, Duration.ZERO, null);
        t2.setId(2);
        Task t3 = new Task("C", "desc", TaskStatus.NEW, Duration.ZERO, null);
        t3.setId(3);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t3);

        historyManager.remove(t1.getId());
        assertEquals(List.of(t2, t3), historyManager.getHistory(), "После удаления первого неверный порядок");

        historyManager.add(t1);
        historyManager.remove(t2.getId());
        assertEquals(List.of(t3, t1), historyManager.getHistory(), "После удаления среднего неверный порядок");

        historyManager.add(t2);
        historyManager.remove(t2.getId());
        assertEquals(List.of(t3, t1), historyManager.getHistory(), "После удаления последнего неверный порядок");
    }

    @Test
    void removingNonexistentIdDoesNotThrow() {
        Task t1 = new Task("A", "desc", TaskStatus.NEW, Duration.ZERO, null);
        t1.setId(1);
        historyManager.add(t1);
        historyManager.remove(t1.getId());
        assertDoesNotThrow(() -> historyManager.remove(t1.getId()),
                "Повторное удаление не должно бросать исключение");
    }
}
