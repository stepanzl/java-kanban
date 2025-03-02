import manager.Managers;
import manager.TaskManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

public class Main {

    public static void main(String[] args) {

        TaskManager manager = Managers.getDefault();
        Task task1 = new Task("Task1", "test task #1", TaskStatus.NEW);
        Task task2 = new Task("Task2", "test task #2", TaskStatus.DONE);
        manager.addTask(task1);
        manager.addTask(task2);
        Epic epic1 = new Epic("Epic1", "test epic #1");
        manager.addEpic(epic1);
        final int epicId1 = epic1.getId();
        Subtask subtask1 = new Subtask("Subtask1", "test subtask #1", TaskStatus.NEW, epicId1);
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask2", "test subtask #2", TaskStatus.IN_PROGRESS, epicId1);
        manager.addSubtask(subtask2);
        Epic epic2 = new Epic("Epic2", "test epic #2");
        manager.addEpic(epic2);
        final int epicId2 = epic2.getId();
        Subtask subtask3 = new Subtask("Subtask3", "test subtask #3", TaskStatus.DONE, epicId2);
        manager.addSubtask(subtask3);

        manager.getTask(task1.getId());
        manager.getTask(task2.getId());
        manager.getSubtask(subtask1.getId());
        manager.getSubtask(subtask2.getId());
        manager.getSubtask(subtask3.getId());
        manager.getEpic(epic1.getId());
        manager.getEpic(epic2.getId());
        manager.getTask(task1.getId());
        manager.getTask(task2.getId());
        manager.getEpic(epic1.getId());
        manager.getSubtask(subtask1.getId());


        printAllTasks(manager);

        System.out.println("\nChanging statuses..");
        task1.setStatus(TaskStatus.DONE);
        manager.updateTask(task1);
        task2.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(task2);
        subtask1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);
        subtask2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask2);
        subtask3.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask3);

        printAllTasks(manager);

        System.out.println("\nRemoving tasks..");
        manager.removeTask(task1.getId());
        manager.removeTask(task2.getId());

        printAllTasks(manager);

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
        System.out.println("\nHistory:");
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }


    }
}
