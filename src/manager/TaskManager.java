package manager;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>();

    private final Map<Integer, Epic> epics = new HashMap<>();

    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    private Integer generateId = 0;

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getAllSubtasks() {
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
        tasks.put(generateId, task);
        task.setId(generateId);
        generateId++;
    }

    public void addEpic(Epic epic) {
        epics.put(generateId, epic);
        epic.setId(generateId);
        generateId++;
    }

    public void addSubtask(Subtask subtask) {
        subtasks.put(generateId, subtask);
        subtask.setId(generateId);
        epics.get(subtask.getEpicId()).getSubtaskIds().add(subtask.getId());
        generateId++;
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        epics.get(subtask.getEpicId()).getSubtaskIds().add(subtask.getId());
    }

    public void removeTask(Task task) {
        tasks.remove(task.getId());
    }

    public void removeEpic(Epic epic) {
        epics.remove(epic.getId());
        for (int i = 0; i < epic.getSubtaskIds().size(); i++) {
            subtasks.remove(epic.getSubtaskIds().get(i));
        }
    }

    public void removeSubtask(Subtask subtask) {
        subtasks.remove(subtask.getId());
        epics.get(subtask.getEpicId()).getSubtaskIds().remove(subtask.getId());
    }

    /*public ArrayList<Subtask> getAllSubtasksByEpic(Epic epic) {

    }*/

}


