package manager;

import tasks.Task;

public class Node {
    Task task;
    Node next;
    Node prev;

    Node(Node prev, Task task, Node next) {
        this.task = task;
        this.next = next;
        this.prev = prev;
    }

    @Override
    public String toString() {
        return "Node{" +
                "task=" + task +
                ", next=" + next +
                ", prev=" + prev +
                '}';
    }
}
