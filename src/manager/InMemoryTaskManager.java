package manager;

import exceptions.NotFoundException;
import exceptions.TasksOverlapException;
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

    private Integer idCount = 1;

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

    private void log(String message) {
        String time = java.time.LocalTime.now().toString();
        System.out.println("[" + time + "] [TASK MANAGER] " + message);
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
    public Optional<Task> getTaskById(int id) throws NotFoundException {
        Task task = tasks.get(id);
        if (task == null) {
            throw new NotFoundException("Task " + id + " not found");
        }
        historyManager.add(task);
        return Optional.of(task);
    }

    @Override
    public Optional<Epic> getEpicById(int id) throws NotFoundException {
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException("Epic " + id + " not found");
        }
        historyManager.add(epic);
        return Optional.of(epic);
    }

    @Override
    public Optional<Subtask> getSubtaskById(int id) throws NotFoundException {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new NotFoundException("Subtask " + id + " not found");
        }
        historyManager.add(subtask);
        return Optional.of(subtask);
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
        log("Создана задача ID=" + taskToAdd.getId() + ": " + taskToAdd.getName());
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
        log("Создан эпик ID=" + epicToAdd.getId() + ": " + epicToAdd.getName());
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
        log("Создана подзадача ID=" + subtaskToAdd.getId() + ": " + subtaskToAdd.getName());
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
        log("Обновлена задача ID=" + taskToUpdate.getId() + ": " + taskToUpdate.getName());
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
    public void updateEpic(Epic epic) {
        Epic epicToUpdate = new Epic(epic);
        Epic oldEpic = epics.get(epic.getId());
        if (oldEpic != null) {
            prioritizedTasks.remove(oldEpic);
        }
        epicToUpdate.setId(epic.getId());
        epics.put(epicToUpdate.getId(), epicToUpdate);
        updateEpicStatus(epicToUpdate.getId());
        updateEpicTimeFields(epicToUpdate.getId());

        log("Обновлён эпик ID=" + epicToUpdate.getId() + ": " + epicToUpdate.getName());
    }

    @Override
    public void deleteTask(int id) throws NotFoundException {
        Task task = tasks.get(id);
        if (task == null) {
            throw new NotFoundException("Task " + id + " not found");
        }
        historyManager.remove(id);
        tasks.remove(id);
        prioritizedTasks.remove(task);
        log("Удалена задача ID=" + id + ": " + task.getName());
    }

    @Override
    public void deleteEpic(int id) throws NotFoundException {
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException("Epic " + id + " not found");
        }

        for (Integer subtaskId : epic.getSubtaskIds()) {
            subtasks.remove(subtaskId);
            historyManager.remove(subtaskId);
            prioritizedTasks.removeIf(task -> task.getId() == subtaskId);
        }

        epics.remove(id);
        historyManager.remove(id);
        log("Удалён эпик ID=" + id + ": " + epic.getName());
    }

    @Override
    public void deleteSubtask(int id) throws NotFoundException {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new NotFoundException("Subtask " + id + " not found");
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
        prioritizedTasks.remove(subtask);
        log("Удалена подзадача ID=" + id + ": " + subtask.getName());
    }

    @Override
    public List<Subtask> getEpicSubtasks(int id) throws NotFoundException {
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException("Epic " + id + " not found");
        }

        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
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
            epic.setDuration(Duration.ZERO);
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