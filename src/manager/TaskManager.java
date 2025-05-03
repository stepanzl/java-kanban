package manager;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.List;
import java.util.Optional;

public interface TaskManager {
    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    Optional<Task> getTask(int id);

    Optional<Epic> getEpic(int id);

    Optional<Subtask> getSubtask(int id);

    void removeAllTasks();

    void removeAllEpics();

    void removeAllSubtasks();

    void addTask(Task task);

    void addEpic(Epic epic);

    void addSubtask(Subtask subtask);

    void updateTask(Task task);

    void updateSubtask(Subtask subtask);

    void removeTask(int id);

    void removeEpic(int id);

    void removeSubtask(int id);

    List<Subtask> getEpicSubtasks(int id);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();

}
