import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class InMemoryTaskManagerTest {

    private TaskManager taskManager;
    private Task task;

    @BeforeEach
    public void beforeEach() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void ShouldAddNewTask() {
        task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        taskManager.addTask(task);

        final Task savedTask = taskManager.getTask(task.getId());

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getAllTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void ShouldAddNewEpic() {
        Epic epic = new Epic("Test addNewEpic", "Test addNewEpic description");
        taskManager.addEpic(epic);

        final Epic savedEpic = taskManager.getEpic(epic.getId());

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");

        final List<Epic> epics = taskManager.getAllEpics();

        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic, epics.get(0), "Эпики не совпадают.");
    }

    @Test
    void ShouldAddNewSubtask() {
        Epic epic = new Epic("Test addNewEpic", "Test addNewEpic description");
        taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Test addNewSubtask", "Test addNewSubtask description", TaskStatus.NEW, epic.getId());
        taskManager.addSubtask(subtask);

        final Subtask savedSubtask = taskManager.getSubtask(subtask.getId());

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают.");

        final List<Subtask> subtasks = taskManager.getAllSubtasks();

        assertNotNull(subtasks, "Подзадачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask, subtasks.get(0), "Подзадачи не совпадают.");
        assertEquals(1, epic.getSubtaskIds().size(), "Неверное количество Id подзадач в эпике.");
        assertEquals(subtask.getId(), epic.getSubtaskIds().getFirst(), "Id подзадачи не совпадают.");
    }


    //проверьте, что объект Epic нельзя добавить в самого себя в виде подзадачи
    //проверьте, что объект Subtask нельзя сделать своим же эпиком

    //тестов нет, т.к. для добавления и изменения подзадач используются методы addSubtask(Subtask subtask)
    // и updateSubtask(Subtask subtask), которые не могут принять объект Epic на вход

    @Test
    void ShouldNotConflictBetweenSetIdAndGeneratedId() {
        Task task1 = new Task("Test task 1", "Test task 1 description", TaskStatus.NEW);
        taskManager.addTask(task1);
        Task task2 = new Task("Test task 2", "Test task 2 description", TaskStatus.DONE);
        task2.setId(task1.getId());

        taskManager.addTask(task2);

        assertNotEquals(task1.getId(), task2.getId(), "Задаче 2 присвоен неверный Id");
    }

    @Test
    void ShouldNotChangeWhenAddedToManager() {
        task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        taskManager.addTask(task);

        final Task savedTask = taskManager.getTask(task.getId());

        assertEquals(task.getId(), savedTask.getId(), "Поля id не совпадают.");
        assertEquals(task.getName(), savedTask.getName(), "Поля name не совпадают.");
        assertEquals(task.getDescription(), savedTask.getDescription(), "Поля description не совпадают.");
        assertEquals(task.getStatus(), savedTask.getStatus(), "Поля status не совпадают.");

    }

    @Test
    void ShouldNotUpdateTaskInHistoryWhenUpdatedInManager() {
        task = new Task("Test task", "Test task description", TaskStatus.NEW);
        taskManager.addTask(task);
        taskManager.getTask(task.getId()); //при вызове getTask задача добавится в historyManager
        task.setStatus(TaskStatus.DONE);
        taskManager.updateTask(task);

        final List<Task> history = taskManager.getHistory();

        assertNotEquals(task.getStatus(), history.getFirst().getStatus(), "Статус задачи в истории изменился");
    }

    @Test
    void ShouldRemoveCorrectTaskById() {
        Task task1 = new Task("Test task #1", "Test task #1 description", TaskStatus.NEW);
        taskManager.addTask(task1);
        Task task2 = new Task("Test task #2", "Test task #2 description", TaskStatus.NEW);
        taskManager.addTask(task2);
        Task task3 = new Task("Test task #3", "Test task #3 description", TaskStatus.NEW);
        taskManager.addTask(task3);

        taskManager.removeTask(task2.getId());
        final List<Task> tasks = taskManager.getAllTasks();

        assertEquals(2, tasks.size(), "Задача не была удалена");
        assertEquals(task1, tasks.get(0), "Задача #1 не должна быть удалена");
        assertEquals(task3, tasks.get(1), "Задача #3 не должна быть удалена");
    }

    @Test
    void ShouldRemoveAllTasks() {
        Task task1 = new Task("Test task #1", "Test task #1 description", TaskStatus.NEW);
        taskManager.addTask(task1);
        Task task2 = new Task("Test task #2", "Test task #2 description", TaskStatus.NEW);
        taskManager.addTask(task2);
        Task task3 = new Task("Test task #3", "Test task #3 description", TaskStatus.NEW);
        taskManager.addTask(task3);

        taskManager.removeAllTasks();
        final List<Task> tasks = taskManager.getAllTasks();

        assertEquals(0, tasks.size(), "Задачи не были удалена");
    }

    @Test
    void ShouldRemoveEpicAndAllItsSubtasks() {
        Epic epic = new Epic("Test Epic", "Test Epic description");
        taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("Test subtask #1", "Test subtask #1 description",
                TaskStatus.NEW, epic.getId());
        taskManager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Test subtask #2", "Test subtask #2 description",
                TaskStatus.NEW, epic.getId());
        taskManager.addSubtask(subtask2);

        taskManager.removeEpic(epic.getId());

        final List<Epic> epics = taskManager.getAllEpics();
        final List<Subtask> subtasks = taskManager.getAllSubtasks();

        assertEquals(0, epics.size(), "Эпик не был удален");
        assertEquals(0, subtasks.size(), "Подзадачи не были удалены");
    }

}
