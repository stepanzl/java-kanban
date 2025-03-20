package manager;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>();

    private final Map<Integer, Epic> epics = new HashMap<>();

    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    private Integer idCount = 0;

    private final HistoryManager historyManager = Managers.getDefaultHistory();


    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public void removeAllTasks() {
        for (Integer i : tasks.keySet()) {
            historyManager.remove(i);
        }
        tasks.clear();
    }

    @Override
    public void removeAllEpics() {
        for (Integer i : epics.keySet()) {
            historyManager.remove(i);
        }
        for (Integer i : subtasks.keySet()) {
            historyManager.remove(i);
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void removeAllSubtasks() {
        for (Integer i : subtasks.keySet()) {
            historyManager.remove(i);
        }
        subtasks.clear();
    }

    @Override
    public void addTask(Task task) {
        Task taskToAdd = new Task(task);
        tasks.put(idCount, taskToAdd);
        taskToAdd.setId(idCount);
        task.setId(idCount);
        idCount++;
    }

    @Override
    public void addEpic(Epic epic) {
        Epic epicToAdd = new Epic(epic);
        epics.put(idCount, epicToAdd);
        epicToAdd.setId(idCount);
        epic.setId(idCount);
        idCount++;
        updateEpicStatus(epicToAdd.getId());
    }

    @Override
    public void addSubtask(Subtask subtask) {
        Subtask subtaskToAdd = new Subtask(subtask);
        subtasks.put(idCount, subtaskToAdd);
        subtaskToAdd.setId(idCount);
        subtask.setId(idCount);
        epics.get(subtaskToAdd.getEpicId()).getSubtaskIds().add(subtaskToAdd.getId());
        idCount++;
        updateEpicStatus(subtaskToAdd.getEpicId());
    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(subtask.getEpicId());
    }

    @Override
    public void removeTask(int id) {
        historyManager.remove(id);
        tasks.remove(id);
    }

    @Override
    public void removeEpic(int id) {
        historyManager.remove(id);
        for (int i = 0; i < epics.get(id).getSubtaskIds().size(); i++) {
            subtasks.remove(epics.get(id).getSubtaskIds().get(i));
            historyManager.remove(epics.get(id).getSubtaskIds().get(i));
        }
        epics.remove(id);
    }

    @Override
    public void removeSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.remove(id);
        epics.get(subtask.getEpicId()).getSubtaskIds().remove(subtask.getId());
        subtasks.remove(id);
        updateEpicStatus(subtask.getEpicId());
    }

    @Override
    public List<Subtask> getAllSubtasksByEpicId(int id) {
        List<Subtask> subtasksByEpic = new ArrayList<>();
        List<Integer> subtaskIdsByEpic = epics.get(id).getSubtaskIds();
        for (int i = 0; i < subtaskIdsByEpic.size(); i++) {
            subtasksByEpic.add(subtasks.get(subtaskIdsByEpic.get(i)));
        }
        return subtasksByEpic;
    }

    @Override
    public void updateEpicStatus(int id) {
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

    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}


