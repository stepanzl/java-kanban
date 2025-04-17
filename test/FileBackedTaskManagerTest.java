import manager.FileBackedTaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void beforeEach() throws IOException {
        tempFile = File.createTempFile("task_manager", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    void afterEach() {
        tempFile.delete();
    }

    @Test
    void shouldSaveAndLoadEmptyFileCorrectly() throws IOException {
        manager.removeAllTasks(); // для вызова save()
        List<String> lines = Files.readAllLines(tempFile.toPath());
        assertEquals(1, lines.size());
        assertEquals("id,type,name,status,description,epic", lines.get(0));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldSaveMultipleTasksToFile() throws IOException {
        Task task1 = new Task("Task1", "Description1", TaskStatus.NEW);
        Task task2 = new Task("Task2", "Description2", TaskStatus.DONE);
        manager.addTask(task1);
        manager.addTask(task2);

        List<String> lines = Files.readAllLines(tempFile.toPath());

        assertEquals(3, lines.size());
        assertTrue(lines.get(1).contains("Task1"));
        assertTrue(lines.get(2).contains("Task2"));
    }

    @Test
    void shouldLoadMultipleTasksFromFile() {
        Task task1 = new Task("Task1", "Description1", TaskStatus.NEW);
        Task task2 = new Task("Task2", "Description2", TaskStatus.DONE);
        manager.addTask(task1);
        manager.addTask(task2);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(2, loadedManager.getAllTasks().size());
        assertEquals(task1.getName(), loadedManager.getTask(task1.getId()).getName());
        assertEquals(task2.getDescription(), loadedManager.getTask(task2.getId()).getDescription());
    }

    @Test
    void shouldRestoreSubtaskIdsInEpicsAfterLoadingFromFile() throws IOException {
        Epic epic = new Epic("Epic1", "Test epic #1");
        manager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask1", "Test subtask #1", TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Subtask2", "Test subtask #2", TaskStatus.DONE, epic.getId());
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Epic loadedEpic = loadedManager.getEpic(epic.getId());

        assertNotNull(loadedEpic);
        List<Integer> subtaskIds = loadedEpic.getSubtaskIds();

        assertEquals(2, subtaskIds.size());
        assertTrue(subtaskIds.contains(subtask1.getId()));
        assertTrue(subtaskIds.contains(subtask2.getId()));
    }

    @Test
    void shouldGenerateNewIdsAfterLoading() {
        Task task1 = new Task("Task1", "Desc", TaskStatus.NEW);
        manager.addTask(task1);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Task task2 = new Task("Task2", "Desc", TaskStatus.NEW);
        loadedManager.addTask(task2);

        assertNotEquals(task1.getId(), task2.getId());
    }

    @Test
    void shouldNotLoadDeletedTasks() {
        Task task = new Task("Task", "Desc", TaskStatus.NEW);
        manager.addTask(task);
        manager.removeTask(task.getId());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getAllTasks().isEmpty());
    }

}