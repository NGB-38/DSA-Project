public class DoublyLinkedList<T> {
    private class Node {
        T data;
        Node next;
        Node prev;

        Node(T data) {
            this.data = data;
        }
    }

    private Node head;
    private Node tail;
    private Node current;
    private int size;

    public void add(T data) {
        Node newNode = new Node(data);
        if (head == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        size++;
    }

    public T get(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
        Node temp = head;
        for (int i = 0; i < index; i++) {
            temp = temp.next;
        }
        return temp.data;
    }

    public void resetCurrent() {
        current = head;
    }

    public T getCurrent() {
        return current != null ? current.data : null;
    }

    public void moveToNext() {
        if (current != null) current = current.next;
    }

    public void moveToPrev() {
        if (current != null) current = current.prev;
    }

    public boolean hasNext() {
        return current != null && current.next != null;
    }

    public boolean hasPrev() {
        return current != null && current.prev != null;
    }

    public int size() {
        return size;
    }
}