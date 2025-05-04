public class Main {

    public static void main(String[] args) {

        /*
        TaskManager manager = Managers.getDefault();
        Task task1 = new Task("Task1", "test task #1", TaskStatus.NEW);
        Task task2 = new Task("Task2", "test task #2", TaskStatus.DONE);
        manager.createTask(task1);
        manager.createTask(task2);
        Epic epic1 = new Epic("Epic1", "test epic #1");
        manager.createEpic(epic1);
        final int epicId1 = epic1.getId();
        Subtask subtask1 = new Subtask("Subtask1", "test subtask #1", TaskStatus.NEW, epicId1);
        manager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask2", "test subtask #2", TaskStatus.IN_PROGRESS, epicId1);
        manager.createSubtask(subtask2);
        Subtask subtask3 = new Subtask("Subtask3", "test subtask #3", TaskStatus.DONE, epicId1);
        manager.createSubtask(subtask3);
        Epic epic2 = new Epic("Epic2", "test epic #2");
        manager.createEpic(epic2);

        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());
        manager.getSubtaskById(subtask1.getId());
        manager.getSubtaskById(subtask2.getId());
        manager.getSubtaskById(subtask3.getId());
        manager.getEpicById(epic1.getId());
        manager.getEpicById(epic2.getId());
        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());
        manager.getEpicById(epic1.getId());
        manager.getSubtaskById(subtask1.getId());

        printAllTasks(manager);

        System.out.println("\nRemoving task1..");
        manager.deleteTask(task1.getId());

        printAllTasks(manager);

        System.out.println("\nRemoving epic..");
        manager.deleteEpic(epic1.getId());

        printAllTasks(manager);

    }

    private static void printAllTasks(TaskManager taskManager) {
        System.out.println("\nTasks:");
        for (Task task : taskManager.getTasks()) {
            System.out.println(task);
        }
        System.out.println("\nEpics:");
        for (Epic epic : taskManager.getEpics()) {
            System.out.println(epic);
        }
        System.out.println("\nSubtasks:");
        for (Subtask subtask : taskManager.getSubtasks()) {
            System.out.println(subtask);
        }
        System.out.println("\nHistory:");
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }


         */

    }
}
