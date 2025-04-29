package manager;

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


public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    private void save() {
        try (BufferedWriter br = new BufferedWriter(new FileWriter(file))) {
            br.write("id,type,name,status,description,epic");
            br.newLine();
            for (Task task : getTasks().values()) {
                br.write(toString(task));
                br.newLine();
            }
            for (Epic epic : getEpics().values()) {
                br.write(toString(epic));
                br.newLine();
            }
            for (Subtask subtask : getSubtasks().values()) {
                br.write(toString(subtask));
                br.newLine();
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
            getEpics().put(task.getId(), (Epic) task);
        } else if (task instanceof Subtask) {
            getSubtasks().put(task.getId(), (Subtask) task);
            int epicId = ((Subtask) task).getEpicId();
            getEpics().get(epicId).getSubtaskIds().add(task.getId());
            updateEpicStatus(epicId);
        } else {
            getTasks().put(task.getId(), task);
        }
    }

    private static String toString(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",");
        sb.append(task.getType()).append(",");
        sb.append(task.getName()).append(",");
        sb.append(task.getStatus()).append(",");
        sb.append(task.getDescription()).append(",");
        if (task instanceof Subtask) {
            sb.append(((Subtask) task).getEpicId()).append(",");
        }
        return sb.toString();
    }

    private static Task fromString(String value) {
        String[] split = value.split(",");
        int id = Integer.parseInt(split[0]);
        TaskType type = TaskType.valueOf(split[1]);
        String name = split[2];
        TaskStatus status = TaskStatus.valueOf(split[3]);
        String description = split[4];

        switch (type) {
            case TASK:
                return new Task(id, name, description, status);
            case EPIC:
                return new Epic(id, name, description, status);
            case SUBTASK:
                int epicId = Integer.parseInt(split[5]);
                return new Subtask(id, name, description, status, epicId);
            default:
                throw new IllegalStateException("Неизвестный тип задачи: " + type);
        }

    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
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
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
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
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        save();
    }

    @Override
    public void removeSubtask(int id) {
        super.removeSubtask(id);
        save();
    }

    public static void main(String[] args) {
        File file = new File("tasks.csv");
        file.deleteOnExit();

        FileBackedTaskManager manager1 = new FileBackedTaskManager(file);

        Task task1 = new Task("Task 1", "test task #1", TaskStatus.NEW);
        manager1.addTask(task1);
        Task task2 = new Task("Task 2", "test task #2", TaskStatus.IN_PROGRESS);
        manager1.addTask(task2);
        Task task3 = new Task("Task 3", "test task #3", TaskStatus.DONE);
        manager1.addTask(task3);
        Epic epic1 = new Epic("Epic 1", "test epic #1");
        manager1.addEpic(epic1);
        final int epicId1 = epic1.getId();
        Subtask subtask1 = new Subtask("Subtask1", "test subtask #1", TaskStatus.NEW, epicId1);
        manager1.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask2", "test subtask #2", TaskStatus.IN_PROGRESS, epicId1);
        manager1.addSubtask(subtask2);
        Epic epic2 = new Epic("Epic 2", "test epic #2");
        manager1.addEpic(epic2);

        FileBackedTaskManager manager2 = FileBackedTaskManager.loadFromFile(file);

        System.out.println("=== Tasks ===");
        manager1.getAllTasks().forEach(System.out::println);
        System.out.println("--- Loaded ---");
        manager2.getAllTasks().forEach(System.out::println);
        System.out.println(manager1.getAllTasks().equals(manager2.getAllTasks()) ? "Tasks match" : "Tasks do not match");
        System.out.println();

        System.out.println("=== Epics ===");
        manager1.getAllEpics().forEach(System.out::println);
        System.out.println("--- Loaded ---");
        manager2.getAllEpics().forEach(System.out::println);
        System.out.println(manager1.getAllEpics().equals(manager2.getAllEpics()) ? "Epics match" : "Epics do not match");
        System.out.println();

        System.out.println("=== Subtasks ===");
        manager2.getAllSubtasks().forEach(System.out::println);
        System.out.println("--- Loaded ---");
        manager2.getAllSubtasks().forEach(System.out::println);
        System.out.println(manager1.getAllSubtasks().equals(manager2.getAllSubtasks()) ? "Subtasks match" : "Subtasks do not match");
    }

}
