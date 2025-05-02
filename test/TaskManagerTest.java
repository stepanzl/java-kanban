import manager.TaskManager;
import manager.TasksOverlapException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    public void setup() {
        manager = createManager();
    }

    @Test
    void ShouldAddNewTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        manager.addTask(task);

        final Task savedTask = manager.getTask(task.getId());

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = manager.getAllTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают.");
    }

    @Test
    void ShouldAddNewEpic() {
        Epic epic = new Epic("Test addNewEpic", "Test addNewEpic description");
        manager.addEpic(epic);

        final Epic savedEpic = manager.getEpic(epic.getId());

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");

        final List<Epic> epics = manager.getAllEpics();

        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic, epics.getFirst(), "Эпики не совпадают.");
    }

    @Test
    void ShouldAddNewSubtask() {
        Epic epic = new Epic("Test addNewEpic", "Test addNewEpic description");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Test addNewSubtask", "Test addNewSubtask description",
                TaskStatus.NEW, epic.getId());
        manager.addSubtask(subtask);

        final Subtask savedSubtask = manager.getSubtask(subtask.getId());

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают.");

        final List<Subtask> subtasks = manager.getAllSubtasks();

        assertNotNull(subtasks, "Подзадачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask, subtasks.getFirst(), "Подзадачи не совпадают.");
        assertEquals(1, manager.getEpic(epic.getId()).getSubtaskIds().size(),
                "Неверное количество Id подзадач в эпике.");
        assertEquals(subtask.getId(), manager.getEpic(epic.getId()).getSubtaskIds().getFirst(),
                "Id подзадачи не совпадают.");
    }

    @Test
    void ShouldNotConflictBetweenSetIdAndGeneratedId() {
        Task task1 = new Task("Test task 1", "Test task 1 description", TaskStatus.NEW);
        manager.addTask(task1);
        Task task2 = new Task("Test task 2", "Test task 2 description", TaskStatus.DONE);
        task2.setId(task1.getId());

        manager.addTask(task2);

        assertNotEquals(manager.getTask(0).getId(), manager.getTask(1).getId(),
                "Задаче 2 присвоен неверный Id");
    }

    @Test
    void ShouldNotChangeWhenAddedToManager() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        manager.addTask(task);

        final Task savedTask = manager.getTask(task.getId());

        assertEquals(task.getId(), savedTask.getId(), "Поля id не совпадают.");
        assertEquals(task.getName(), savedTask.getName(), "Поля name не совпадают.");
        assertEquals(task.getDescription(), savedTask.getDescription(), "Поля description не совпадают.");
        assertEquals(task.getStatus(), savedTask.getStatus(), "Поля status не совпадают.");
    }

    @Test
    void ShouldNotChangeIdWhenAddedToManager() {
        Task task = new Task("Test task 1", "Test task 1 description", TaskStatus.NEW);
        manager.addTask(task);
        int taskId = task.getId();
        task.setId(10);

        final Task savedTask = manager.getTask(taskId);

        assertNotEquals(10, savedTask.getId(), "Сеттер не должен менять данные внутри менеджера");
    }

    @Test
    void ShouldNotUpdateTaskInHistoryWhenUpdatedInManager() {
        Task task = new Task("Test task", "Test task description", TaskStatus.NEW);
        manager.addTask(task);
        manager.getTask(task.getId()); // при вызове getTask задача добавится в historyManager
        task.setStatus(TaskStatus.DONE);
        manager.updateTask(task);

        final List<Task> history = manager.getHistory();

        assertNotEquals(task.getStatus(), history.getFirst().getStatus(), "Статус задачи в истории изменился");
    }

    @Test
    void ShouldRemoveCorrectTaskById() {
        Task task1 = new Task("Test task #1", "Test task #1 description", TaskStatus.NEW);
        manager.addTask(task1);
        Task task2 = new Task("Test task #2", "Test task #2 description", TaskStatus.NEW);
        manager.addTask(task2);
        Task task3 = new Task("Test task #3", "Test task #3 description", TaskStatus.NEW);
        manager.addTask(task3);

        manager.getTask(task1.getId());
        manager.getTask(task2.getId());
        manager.getTask(task3.getId());

        manager.removeTask(task2.getId());
        final List<Task> tasks = manager.getAllTasks();

        assertEquals(2, tasks.size(), "Задача не была удалена");
        assertEquals(task1, tasks.get(0), "Задача #1 не должна быть удалена");
        assertEquals(task3, tasks.get(1), "Задача #3 не должна быть удалена");
    }

    @Test
    void ShouldRemoveAllTasks() {
        Task task1 = new Task("Test task #1", "Test task #1 description", TaskStatus.NEW);
        manager.addTask(task1);
        manager.getTask(task1.getId());
        Task task2 = new Task("Test task #2", "Test task #2 description", TaskStatus.NEW);
        manager.addTask(task2);
        manager.getTask(task2.getId());
        Task task3 = new Task("Test task #3", "Test task #3 description", TaskStatus.NEW);
        manager.addTask(task3);
        manager.getTask(task3.getId());

        manager.removeAllTasks();
        final List<Task> tasks = manager.getAllTasks();
        final List<Task> historyList = manager.getHistory();

        assertEquals(0, tasks.size(), "Задачи не были удалены");
        assertEquals(0, historyList.size(), "Просмотры в истории не были удалены");
    }

    @Test
    void ShouldRemoveEpicAndAllItsSubtasks() {
        Epic epic = new Epic("Test Epic", "Test Epic description");
        manager.addEpic(epic);
        manager.getEpic(epic.getId());
        Subtask subtask1 = new Subtask("Test subtask #1", "Test subtask #1 description",
                TaskStatus.NEW, epic.getId());
        manager.addSubtask(subtask1);
        manager.getSubtask(subtask1.getId());
        Subtask subtask2 = new Subtask("Test subtask #2", "Test subtask #2 description",
                TaskStatus.NEW, epic.getId());
        manager.addSubtask(subtask2);
        manager.getSubtask(subtask2.getId());

        manager.removeEpic(epic.getId());

        final List<Epic> epics = manager.getAllEpics();
        final List<Subtask> subtasks = manager.getAllSubtasks();
        final List<Task> historyList = manager.getHistory();

        assertEquals(0, epics.size(), "Эпик не был удален");
        assertEquals(0, subtasks.size(), "Подзадачи не были удалены");
        assertEquals(0, historyList.size(), "Просмотры в истории не были удалены");
    }

    @Test
    void ShouldRemoveAllEpicsAndTheirsSubtasks() {
        Epic epic1 = new Epic("Epic1", "test epic #1");
        manager.addEpic(epic1);
        manager.getEpic(epic1.getId());
        Subtask subtask1 = new Subtask("Subtask1", "test subtask #1", TaskStatus.NEW, epic1.getId());
        manager.addSubtask(subtask1);
        manager.getSubtask(subtask1.getId());
        Subtask subtask2 = new Subtask("Subtask2", "test subtask #2", TaskStatus.IN_PROGRESS,
                epic1.getId());
        manager.addSubtask(subtask2);
        manager.getSubtask(subtask2.getId());
        Epic epic2 = new Epic("Epic2", "test epic #2");
        manager.addEpic(epic2);
        manager.getEpic(epic2.getId());

        manager.removeAllEpics();
        final List<Epic> epics = manager.getAllEpics();
        final List<Subtask> subtasks = manager.getAllSubtasks();
        final List<Task> historyList = manager.getHistory();

        assertEquals(0, epics.size(), "Эпики не были удалены");
        assertEquals(0, subtasks.size(), "Подзадачи не были удалены");
        assertEquals(0, historyList.size(), "Просмотры в истории не были удалены");
    }

    @Test
    void removeAllSubtasks() {
        Epic epic1 = new Epic("Epic1", "test epic #1");
        manager.addEpic(epic1);
        manager.getEpic(epic1.getId());
        Subtask subtask1 = new Subtask("Subtask1", "test subtask #1", TaskStatus.NEW, epic1.getId());
        manager.addSubtask(subtask1);
        manager.getSubtask(subtask1.getId());
        Subtask subtask2 = new Subtask("Subtask2", "test subtask #2", TaskStatus.IN_PROGRESS,
                epic1.getId());
        manager.addSubtask(subtask2);
        manager.getSubtask(subtask2.getId());
        Epic epic2 = new Epic("Epic2", "test epic #2");
        manager.addEpic(epic2);
        manager.getEpic(epic2.getId());

        manager.removeAllSubtasks();
        final List<Epic> epics = manager.getAllEpics();
        final List<Subtask> subtasks = manager.getAllSubtasks();
        final List<Task> historyList = manager.getHistory();

        assertEquals(2, epics.size(), "Эпики не должны быть удалены");
        assertEquals(0, subtasks.size(), "Подзадачи не были удалены");
        assertEquals(2, historyList.size(), "Просмотры подзадач в истории не были удалены");
    }

    @Test
    void epicTimeFieldsShouldBeCalculatedFromSubtasks() {
        Epic epic = new Epic("Epic1", "test epic #1");
        manager.addEpic(epic);

        Subtask sub1 = new Subtask("Sub 1", "desc", TaskStatus.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2025, 5, 1, 10, 0), epic.getId());
        Subtask sub2 = new Subtask("Sub 2", "desc", TaskStatus.NEW, Duration.ofMinutes(90),
                LocalDateTime.of(2025, 5, 1, 11, 0), epic.getId());
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        Epic updatedEpic = manager.getEpic(epic.getId());

        assertEquals(LocalDateTime.of(2025, 5, 1, 10, 0),
                updatedEpic.getStartTime());
        assertEquals(LocalDateTime.of(2025, 5, 1, 12, 30),
                updatedEpic.getEndTime());
        assertEquals(Duration.ofMinutes(120), updatedEpic.getDuration());
    }

    @Test
    void epicTimeFieldsShouldBeNullIfNoSubtasks() {
        Epic epic = new Epic("Epic1", "test epic #1");
        manager.addEpic(epic);

        Epic updatedEpic = manager.getEpic(epic.getId());

        assertNull(updatedEpic.getStartTime());
        assertNull(updatedEpic.getEndTime());
        assertEquals(Duration.ZERO, updatedEpic.getDuration());
    }

    @Test
    void updatingSubtaskShouldRecalculateEpicTimeFields() {
        Epic epic = new Epic("Epic", "desc");
        manager.addEpic(epic);

        Subtask sub = new Subtask("Sub", "desc", TaskStatus.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2025, 5, 1, 10, 0), epic.getId());
        manager.addSubtask(sub);

        sub.setStartTime(LocalDateTime.of(2025, 5, 1, 12, 0));
        sub.setDuration(Duration.ofMinutes(45));
        manager.updateSubtask(sub);

        Epic updatedEpic = manager.getEpic(epic.getId());
        assertEquals(LocalDateTime.of(2025, 5, 1, 12, 0), updatedEpic.getStartTime());
        assertEquals(LocalDateTime.of(2025, 5, 1, 12, 45), updatedEpic.getEndTime());
        assertEquals(Duration.ofMinutes(45), updatedEpic.getDuration());
    }

    @Test
    void deletingLastSubtaskShouldResetEpicTimeFields() {
        Epic epic = new Epic("Epic", "desc");
        manager.addEpic(epic);

        Subtask sub = new Subtask("Sub", "desc", TaskStatus.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2025, 5, 1, 10, 0), epic.getId());
        manager.addSubtask(sub);

        manager.removeSubtask(sub.getId());

        Epic updatedEpic = manager.getEpic(epic.getId());
        assertNull(updatedEpic.getStartTime());
        assertNull(updatedEpic.getEndTime());
        assertEquals(Duration.ZERO, updatedEpic.getDuration());
    }

    @Test
    void deletingEpicShouldAlsoDeleteSubtasks() {
        Epic epic = new Epic("Epic", "desc");
        manager.addEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "desc", TaskStatus.NEW, epic.getId());
        Subtask sub2 = new Subtask("Sub2", "desc", TaskStatus.NEW, epic.getId());
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        manager.removeEpic(epic.getId());

        assertNull(manager.getSubtask(sub1.getId()));
        assertNull(manager.getSubtask(sub2.getId()));
        assertTrue(manager.getAllSubtasks().isEmpty(), "Все подзадачи должны быть удалены");
    }


    @Test
    void shouldNotAddSubtaskWithNonexistentEpic() {
        Subtask subtask = new Subtask("Subtask title", "description", TaskStatus.NEW, 123);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> manager.addSubtask(subtask)
        );

        assertEquals("Epic с ID 123 не существует.", exception.getMessage());
    }

    @Test
    void epicStatusShouldReflectSubtasksStatus() {
        Epic epic = new Epic("Status Epic", "Testing status calculation");
        manager.addEpic(epic);

        Subtask sub1 = new Subtask("Sub 1", "desc", TaskStatus.NEW, epic.getId());
        Subtask sub2 = new Subtask("Sub 2", "desc", TaskStatus.NEW, epic.getId());
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        assertEquals(TaskStatus.NEW, manager.getEpic(epic.getId()).getStatus(), "Ожидался статус NEW");

        sub1.setStatus(TaskStatus.DONE);
        sub2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(sub1);
        manager.updateSubtask(sub2);

        assertEquals(TaskStatus.DONE, manager.getEpic(epic.getId()).getStatus(), "Ожидался статус DONE");

        sub1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(sub1);

        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(epic.getId()).getStatus(), "Ожидался статус IN_PROGRESS");
    }

    @Test
    void shouldThrowExceptionIfTasksOverlap() {
        Task task1 = new Task("Task 1", "desc", TaskStatus.NEW, Duration.ofMinutes(60),
                LocalDateTime.of(2025, 5, 1, 10, 0));
        manager.addTask(task1);

        Task overlappingTask = new Task("Task 2", "desc", TaskStatus.NEW, Duration.ofMinutes(45),
                LocalDateTime.of(2025, 5, 1, 10, 30));

        TasksOverlapException exception = assertThrows(
                TasksOverlapException.class,
                () -> manager.addTask(overlappingTask)
        );

        assertEquals("Задача пересекается по времени с другой задачей", exception.getMessage());
    }

    @Test
    void shouldAllowTasksWithAdjacentTimeRanges() {
        Task task1 = new Task("Task 1", "desc", TaskStatus.NEW, Duration.ofMinutes(60),
                LocalDateTime.of(2025, 5, 1, 10, 0));
        manager.addTask(task1);

        Task adjacentTask = new Task("Task 1", "desc", TaskStatus.NEW, Duration.ofMinutes(60),
                LocalDateTime.of(2025, 5, 1, 11, 0));

        assertDoesNotThrow(() -> manager.addTask(adjacentTask));
    }

    @Test
    void getPrioritizedTasksTest() {
        Task task1 = new Task("Task 1", "desc 1", TaskStatus.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2025, 5, 2, 10, 0));
        Task task2 = new Task("Task 2", "desc 2", TaskStatus.NEW, Duration.ofMinutes(45),
                LocalDateTime.of(2025, 5, 2, 9, 0));
        Task task3 = new Task("Task 3", "task without time", TaskStatus.NEW, null, null);
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);

        List<Task> prioritized = manager.getPrioritizedTasks();

        assertFalse(prioritized.contains(task3), "Задача без времени не должна быть в списке");
        assertEquals(List.of(task2, task1), prioritized,
                "Задачи должны быть отсортированы по времени начала");
    }
}