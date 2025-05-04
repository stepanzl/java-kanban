import manager.FileBackedTaskManager;
import manager.ManagerSaveException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tempFile;

    @Override
    protected FileBackedTaskManager createManager() {
        try {
            tempFile = File.createTempFile("task_manager", ".csv");
        } catch (IOException e) {
            fail("Ошибка при создании временного файла");
        }
        return new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    void tearDown() {
        tempFile.delete();
    }

    @Test
    void shouldSaveAndLoadEmptyFileCorrectly() throws IOException {
        manager.removeAllTasks(); // вызываем save()
        List<String> lines = Files.readAllLines(tempFile.toPath());
        assertEquals(1, lines.size());
        assertEquals("id,type,name,status,description,duration,startTime,epic", lines.getFirst());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loadedManager.getTasks().isEmpty());
        assertTrue(loadedManager.getEpics().isEmpty());
        assertTrue(loadedManager.getSubtasks().isEmpty());
    }

    @Test
    void shouldSaveMultipleTasksToFile() throws IOException {
        Task task1 = new Task("Task1", "Description1", TaskStatus.NEW);
        Task task2 = new Task("Task2", "Description2", TaskStatus.DONE);
        manager.createTask(task1);
        manager.createTask(task2);

        List<String> lines = Files.readAllLines(tempFile.toPath());

        assertEquals(3, lines.size()); // заголовок + 2 задачи
        assertTrue(lines.get(1).contains("Task1"));
        assertTrue(lines.get(2).contains("Task2"));
    }

    @Test
    void shouldLoadMultipleTasksFromFile() {
        Task task1 = new Task("Task1", "Description1", TaskStatus.NEW);
        Task task2 = new Task("Task2", "Description2", TaskStatus.DONE);
        manager.createTask(task1);
        manager.createTask(task2);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(2, loadedManager.getTasks().size());
        assertEquals(task1.getName(), loadedManager.getTaskById(task1.getId()).orElseThrow().getName());
        assertEquals(task2.getDescription(), loadedManager.getTaskById(task2.getId()).orElseThrow().getDescription());
    }

    @Test
    void shouldRestoreSubtaskIdsInEpicsAfterLoadingFromFile() {
        Epic epic = new Epic("Epic1", "Test epic #1");
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask1", "Test subtask #1", TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Subtask2", "Test subtask #2", TaskStatus.DONE, epic.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Epic loadedEpic = loadedManager.getEpicById(epic.getId()).orElseThrow();

        List<Integer> subtaskIds = loadedEpic.getSubtaskIds();

        assertEquals(2, subtaskIds.size());
        assertTrue(subtaskIds.contains(subtask1.getId()));
        assertTrue(subtaskIds.contains(subtask2.getId()));
    }

    @Test
    void shouldGenerateNewIdsAfterLoading() {
        Task task1 = new Task("Task1", "Desc", TaskStatus.NEW);
        manager.createTask(task1);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Task task2 = new Task("Task2", "Desc", TaskStatus.NEW);
        loadedManager.createTask(task2);

        assertNotEquals(task1.getId(), task2.getId());
    }

    @Test
    void shouldNotLoadDeletedTasks() {
        Task task = new Task("Task", "Desc", TaskStatus.NEW);
        manager.createTask(task);
        manager.deleteTask(task.getId());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getTasks().isEmpty());
        assertTrue(loadedManager.getTaskById(task.getId()).isEmpty());
    }

    @Test
    void shouldSaveAndLoadTimeFieldsCorrectly() {
        Epic epic = new Epic("Epic1", "test epic #1");
        manager.createEpic(epic);

        Subtask sub = new Subtask("Sub", "desc", TaskStatus.NEW, Duration.ofMinutes(45),
                LocalDateTime.of(2025, 5, 1, 10, 0), epic.getId());
        manager.createSubtask(sub);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        Epic loadedEpic = loaded.getEpicById(epic.getId()).orElseThrow();

        assertEquals(LocalDateTime.of(2025, 5, 1, 10, 0), loadedEpic.getStartTime());
        assertEquals(LocalDateTime.of(2025, 5, 1, 10, 45), loadedEpic.getEndTime());
        assertEquals(Duration.ofMinutes(45), loadedEpic.getDuration());
    }

    @Test
    void shouldLoadEmptyEpicCorrectly() {
        Epic epic = new Epic("Empty Epic", "No subtasks");
        manager.createEpic(epic);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Optional<Epic> loadedEpic = loadedManager.getEpicById(epic.getId());

        assertTrue(loadedEpic.isPresent());
        assertEquals(epic.getName(), loadedEpic.get().getName());
        assertTrue(loadedEpic.get().getSubtaskIds().isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenFileNotExists() {
        File nonExistentFile = new File("nonexistent_file.csv");

        ManagerSaveException exception = assertThrows(
                ManagerSaveException.class,
                () -> FileBackedTaskManager.loadFromFile(nonExistentFile)
        );

        assertEquals("Файл не существует: nonexistent_file.csv", exception.getMessage());
    }
}