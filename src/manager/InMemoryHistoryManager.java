package manager;

import tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final List<Task> history = new ArrayList<>();

    @Override
    public void addToHistory(Task task) {
        Task taskToAdd = new Task(task.getName(), task.getDescription(), task.getStatus());
        if (history.size() == 10) {
            history.removeFirst();
        }
        history.add(taskToAdd);
    }

    @Override
    public List<Task> getHistory() {
        return history;
    }
}
