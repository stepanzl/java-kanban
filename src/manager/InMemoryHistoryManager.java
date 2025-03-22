package manager;

import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private final TaskLinkedList history = new TaskLinkedList();
    public final Map<Integer, Node> historyMap = new HashMap<>();

    @Override
    public void add(Task task) {
        Task taskToAdd = new Task(task);
        if (historyMap.containsKey(taskToAdd.getId())) {
            removeNode(historyMap.get(taskToAdd.getId()));
        }
        history.linkLast(taskToAdd);
        historyMap.put(taskToAdd.getId(), history.tail);
    }

    @Override
    public List<Task> getHistory() {
        return history.getTasks();
    }

    @Override
    public void remove(int id) {
        if (historyMap.containsKey(id)) {
            removeNode(historyMap.get(id));
        }
    }

    private void removeNode(Node node) {
        if (node.next == null && node.prev == null) {
            history.head = null;
            history.tail = null;
        } else if (node.next == null) {
            node.prev.next = null;
            history.tail = node.prev;
        } else if (node.prev == null) {
            node.next.prev = null;
            history.head = node.next;
        } else {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }
        historyMap.remove(node.task.getId());
    }

    class TaskLinkedList {
        public Node head;
        public Node tail;

        void linkLast(Task task) {
            Node t = tail;
            Node newNode = new Node(t, task, null);
            tail = newNode;
            if (t == null)
                head = newNode;
            else
                t.next = newNode;
        }

        List<Task> getTasks() {
            List<Task> tasks = new ArrayList<>();
            Node currentNode = head;
            while (currentNode != null) {
                tasks.add(currentNode.task);
                currentNode = currentNode.next;
            }
            return tasks;
        }

    }
}