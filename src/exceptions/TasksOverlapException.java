package exceptions;

public class TasksOverlapException extends RuntimeException {
    public TasksOverlapException(String message) {
        super(message);
    }
}
