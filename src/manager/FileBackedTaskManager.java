package manager;

import exceptions.ManagerSaveException;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;
import tasks.TaskType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;


public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    private void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("id,type,name,status,description,duration,startTime,epic");
            bw.newLine();
            for (Task task : getTasksMap().values()) {
                bw.write(toString(task));
                bw.newLine();
            }
            for (Epic epic : getEpicsMap().values()) {
                bw.write(toString(epic));
                bw.newLine();
            }
            for (Subtask subtask : getSubtasksMap().values()) {
                bw.write(toString(subtask));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл", e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        int maxId = 0;

        if (!file.exists()) {
            throw new ManagerSaveException("Файл не существует: " + file.getPath());
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                Task task = fromString(line);
                manager.addTaskByType(task);
                maxId = Math.max(maxId, task.getId());
            }
            manager.setIdCount(maxId + 1);

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке из файла", e);
        }
        return manager;
    }

    private void addTaskByType(Task task) {
        if (task instanceof Epic) {
            getEpicsMap().put(task.getId(), (Epic) task);
        } else if (task instanceof Subtask) {
            getSubtasksMap().put(task.getId(), (Subtask) task);
            int epicId = ((Subtask) task).getEpicId();
            getEpicsMap().get(epicId).getSubtaskIds().add(task.getId());
            updateEpicStatus(epicId);
            updateEpicTimeFields(epicId);
        } else {
            getTasksMap().put(task.getId(), task);
        }
    }

    private static String toString(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",");
        sb.append(task.getType()).append(",");
        sb.append(task.getName()).append(",");
        sb.append(task.getStatus()).append(",");
        sb.append(task.getDescription()).append(",");
        Duration duration = task.getDuration();
        sb.append(duration != null ? duration.toMinutes() : "").append(",");
        LocalDateTime startTime = task.getStartTime();
        sb.append(startTime != null ? startTime : "").append(",");
        if (task instanceof Subtask) {
            sb.append(((Subtask) task).getEpicId()).append(",");
        }
        return sb.toString();
    }

    private static Task fromString(String value) {
        String[] split = value.split(",", -1);
        int id = Integer.parseInt(split[0]);
        TaskType type = TaskType.valueOf(split[1]);
        String name = split[2];
        TaskStatus status = TaskStatus.valueOf(split[3]);
        String description = split[4];
        Duration duration = split[5].isEmpty() ? null : Duration.ofMinutes(Long.parseLong(split[5]));
        LocalDateTime startTime = split[6].isEmpty() ? null : LocalDateTime.parse(split[6]);

        switch (type) {
            case TASK:
                return new Task(id, name, description, status, duration, startTime);
            case EPIC:
                return new Epic(id, name, description, status, duration, startTime);
            case SUBTASK:
                int epicId = Integer.parseInt(split[7]);
                return new Subtask(id, name, description, status, duration, startTime, epicId);
            default:
                throw new IllegalStateException("Неизвестный тип задачи: " + type);
        }

    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }

    @Override
    public void removeAllSubtasks() {
        super.removeAllSubtasks();
        save();
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

}
