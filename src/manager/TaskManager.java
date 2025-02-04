package manager;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>();

    private final Map<Integer, Epic> epics = new HashMap<>();

    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    private Integer idCount = 0;

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void removeAllTasks() {
        tasks.clear();
    }

    public void removeAllEpics() {
        epics.clear();
    }

    public void removeAllSubtasks() {
        subtasks.clear();
    }

    public void addTask(Task task) {
        tasks.put(idCount, task);
        task.setId(idCount);
        idCount++;
    }

    public void addEpic(Epic epic) {
        epics.put(idCount, epic);
        epic.setId(idCount);
        idCount++;
        updateEpicStatus(epic.getId());
    }

    public void addSubtask(Subtask subtask) {
        subtasks.put(idCount, subtask);
        subtask.setId(idCount);
        epics.get(subtask.getEpicId()).getSubtaskIds().add(subtask.getId());
        idCount++;
        updateEpicStatus(subtask.getEpicId());
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(subtask.getEpicId());
    }

    public void removeTask(int id) {
        tasks.remove(id);
    }

    public void removeEpic(int id) {

        for (int i = 0; i < epics.get(id).getSubtaskIds().size(); i++) {
            subtasks.remove(epics.get(id).getSubtaskIds().get(i));
        }
        epics.remove(id);
    }

    public void removeSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        epics.get(subtask.getEpicId()).getSubtaskIds().remove(subtask.getId());
        subtasks.remove(id);
        updateEpicStatus(subtask.getEpicId());
    }

    public List<Subtask> getAllSubtasksByEpicId(int id) {
        List<Subtask> subtasksByEpic = new ArrayList<>();
        List<Integer> subtaskIdsByEpic = epics.get(id).getSubtaskIds();
        for (int i = 0; i < subtaskIdsByEpic.size(); i++) {
            subtasksByEpic.add(subtasks.get(subtaskIdsByEpic.get(i)));
        }
        return subtasksByEpic;
    }

    private void updateEpicStatus(int id) {
        List<Subtask> subtaskByEpic = getAllSubtasksByEpicId(id);
        if (subtaskByEpic.isEmpty()) {
            epics.get(id).setStatus(TaskStatus.NEW);
            return;
        }
        boolean foundNewSubtask = false;
        boolean foundDoneSubtask = false;
        boolean foundInProgressSubtask = false;
        for (Subtask subtask : subtaskByEpic) {
            if (subtask.getStatus() == TaskStatus.DONE) {
                foundDoneSubtask = true;
            } else if (subtask.getStatus() == TaskStatus.IN_PROGRESS) {
                foundInProgressSubtask = true;
            } else if (subtask.getStatus() == TaskStatus.NEW) {
                foundNewSubtask = true;
            }
        }
        if (foundNewSubtask && !foundInProgressSubtask && !foundDoneSubtask) {
            epics.get(id).setStatus(TaskStatus.NEW);
        } else if (!foundNewSubtask && !foundInProgressSubtask && foundDoneSubtask) {
            epics.get(id).setStatus(TaskStatus.DONE);
        } else {
            epics.get(id).setStatus(TaskStatus.IN_PROGRESS);
        }
    }
}


