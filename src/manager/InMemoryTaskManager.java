package manager;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>();

    private final Map<Integer, Epic> epics = new HashMap<>();

    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    private Integer idCount = 0;

    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private final Comparator<Task> comparator = Comparator.comparing(Task::getStartTime);

    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(comparator);

    protected Map<Integer, Task> getTasksMap() {
        return tasks;
    }

    protected Map<Integer, Epic> getEpicsMap() {
        return epics;
    }

    protected Map<Integer, Subtask> getSubtasksMap() {
        return subtasks;
    }

    protected void setIdCount(Integer idCount) {
        this.idCount = idCount;
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Optional<Task> getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return Optional.ofNullable(task);
    }

    @Override
    public Optional<Epic> getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return Optional.ofNullable(epic);
    }

    @Override
    public Optional<Subtask> getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return Optional.ofNullable(subtask);
    }

    @Override
    public void removeAllTasks() {
        tasks.keySet().forEach(historyManager::remove);
        tasks.clear();
    }

    @Override
    public void removeAllEpics() {
        epics.keySet().forEach(historyManager::remove);
        subtasks.keySet().forEach(historyManager::remove);
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void removeAllSubtasks() {
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.clear();
    }

    @Override
    public void createTask(Task task) {
        Task taskToAdd = new Task(task);
        taskToAdd.setId(idCount);
        task.setId(idCount);
        if (hasOverlaps(taskToAdd)) {
            throw new TasksOverlapException("Задача пересекается по времени с другой задачей");
        }
        tasks.put(idCount, taskToAdd);
        idCount++;
        addToPrioritizedTasks(taskToAdd);
    }

    @Override
    public void createEpic(Epic epic) {
        Epic epicToAdd = new Epic(epic);
        epicToAdd.setId(idCount);
        epic.setId(idCount);
        epics.put(idCount, epicToAdd);
        idCount++;
        updateEpicStatus(epicToAdd.getId());
        updateEpicTimeFields(epicToAdd.getId());
    }

    @Override
    public void createSubtask(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Epic с ID " + subtask.getEpicId() + " не существует.");
        }
        Subtask subtaskToAdd = new Subtask(subtask);
        subtaskToAdd.setId(idCount);
        subtask.setId(idCount);
        if (hasOverlaps(subtaskToAdd)) {
            throw new TasksOverlapException("Задача пересекается по времени с другой задачей");
        }
        subtasks.put(idCount, subtaskToAdd);
        idCount++;
        epics.get(subtaskToAdd.getEpicId()).getSubtaskIds().add(subtaskToAdd.getId());
        updateEpicStatus(subtaskToAdd.getEpicId());
        updateEpicTimeFields(subtaskToAdd.getEpicId());
        addToPrioritizedTasks(subtaskToAdd);
    }

    @Override
    public void updateTask(Task task) {
        Task taskToUpdate = new Task(task);
        Task oldTask = tasks.get(task.getId());
        if (oldTask != null) {
            prioritizedTasks.remove(oldTask);
        }
        if (hasOverlaps(taskToUpdate)) {
            throw new TasksOverlapException("Задача пересекается по времени с другой задачей");
        }
        tasks.put(taskToUpdate.getId(), taskToUpdate);
        addToPrioritizedTasks(taskToUpdate);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Subtask subtaskToUpdate = new Subtask(subtask);
        Subtask oldSubtask = subtasks.get(subtask.getId());
        if (oldSubtask != null) {
            prioritizedTasks.remove(oldSubtask);
        }
        if (hasOverlaps(subtaskToUpdate)) {
            throw new TasksOverlapException("Задача пересекается по времени с другой задачей");
        }
        subtasks.put(subtaskToUpdate.getId(), subtaskToUpdate);
        updateEpicStatus(subtaskToUpdate.getEpicId());
        updateEpicTimeFields(subtaskToUpdate.getEpicId());
        addToPrioritizedTasks(subtaskToUpdate);
    }

    @Override
    public void deleteTask(int id) {
        if (tasks.containsKey(id)) {
            historyManager.remove(id);
            tasks.remove(id);
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
            epics.remove(id);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            return;
        }

        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            epic.getSubtaskIds().remove((Integer) id);
            updateEpicStatus(epicId);
            updateEpicTimeFields(epicId);
        }

        subtasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public List<Subtask> getEpicSubtasks(int id) {
        return epics.get(id).getSubtaskIds().stream()
                .map(subtasks::get)
                .toList();
    }

    protected void updateEpicStatus(int id) {
        List<Subtask> subtasks = getEpicSubtasks(id);
        Epic epic = epics.get(id);

        if (subtasks.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        long newCount = subtasks.stream()
                .filter(s -> s.getStatus() == TaskStatus.NEW)
                .count();
        long doneCount = subtasks.stream()
                .filter(s -> s.getStatus() == TaskStatus.DONE)
                .count();

        if (doneCount == subtasks.size()) {
            epic.setStatus(TaskStatus.DONE);
        } else if (newCount == subtasks.size()) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    protected void updateEpicTimeFields(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return;
        }

        List<Subtask> subtaskList = getEpicSubtasks(id);

        if (subtaskList.isEmpty()) {
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(Duration.ZERO); // ← Duration.ZERO вместо null
            return;
        }

        LocalDateTime start = subtaskList.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime end = subtaskList.stream()
                .map(subtask -> {
                    LocalDateTime s = subtask.getStartTime();
                    Duration d = subtask.getDuration();
                    return (s != null && d != null) ? s.plus(d) : null;
                })
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        Duration totalDuration = subtaskList.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

        epic.setStartTime(start);
        epic.setEndTime(end);
        epic.setDuration(totalDuration);
    }

    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void addToPrioritizedTasks(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private boolean isOverlapping(Task t1, Task t2) {
        if (t1.getStartTime() == null || t1.getEndTime() == null
                || t2.getStartTime() == null || t2.getEndTime() == null) {
            return false;
        }
        return t1.getStartTime().isBefore(t2.getEndTime())
                && t2.getStartTime().isBefore(t1.getEndTime());
    }


    private boolean hasOverlaps(Task newTask) {
        return getPrioritizedTasks().stream()
                .anyMatch(existingTask -> !existingTask.equals(newTask) && isOverlapping(existingTask, newTask));
    }

}


