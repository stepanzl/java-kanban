package manager;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.List;
import java.util.Optional;

public interface TaskManager {
    List<Task> getTasks();

    Optional<Task> getTaskById(int id);

    void createTask(Task task);

    void updateTask(Task task);

    void deleteTask(int id);

    List<Subtask> getSubtasks();

    Optional<Subtask> getSubtaskById(int id);

    void createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteSubtask(int id);

    List<Epic> getEpics();

    Optional<Epic> getEpicById(int id);

    List<Subtask> getEpicSubtasks(int id);

    void createEpic(Epic epic);

    void deleteEpic(int id);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();

    void removeAllTasks();

    void removeAllEpics();

    void removeAllSubtasks();

}
