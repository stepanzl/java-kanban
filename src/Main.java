import manager.TaskManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = new TaskManager();
        Task task1 = new Task("Task1", "test task #1", TaskStatus.NEW);
        Task task2 = new Task("Task2", "test task #2", TaskStatus.NEW);
        Epic epic1 = new Epic("Epic1", "test epic #1", TaskStatus.NEW);
        Subtask subtask1 = new Subtask("Subtask1", "subtask1", TaskStatus.NEW, 2);
        Subtask subtask2 = new Subtask("Subtask2", "subtask2", TaskStatus.NEW, 2);
        Epic epic2 = new Epic("Epic2", "test epic #2", TaskStatus.NEW);
        Subtask subtask3 = new Subtask("Subtask3", "subtask3", TaskStatus.NEW, 3);


        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);
        printAllTasks(taskManager);

    }

    private static void printAllTasks(TaskManager taskManager) {
        System.out.println("\nTasks:");
        for (Task task : taskManager.getAllTasks()) {
            System.out.println(task);
        }
        System.out.println("\nEpics:");
        for (Epic epic : taskManager.getAllEpics()) {
            System.out.println(epic);
        }
        System.out.println("\nSubtasks:");
        for (Subtask subtask : taskManager.getAllSubtasks()) {
            System.out.println(subtask);
        }
    }
}
