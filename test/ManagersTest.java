import manager.HistoryManager;
import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void getDefault() {
        TaskManager manager = Managers.getDefault();

        assertNotNull(manager);
        assertInstanceOf(TaskManager.class, manager);

    }

    @Test
    void getDefaultHistory() {
        HistoryManager manager = Managers.getDefaultHistory();
        assertNotNull(manager);
        assertInstanceOf(HistoryManager.class, manager);
    }
}