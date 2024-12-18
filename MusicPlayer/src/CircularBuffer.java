import java.lang.reflect.Array;

public class CircularBuffer<T> {
    private T[] buffer;
    private int head;
    private int tail;
    private int size;
    private int capacity;
    private Class<T> type;

    @SuppressWarnings("unchecked")
    public CircularBuffer(int capacity, Class<T> type) {
        this.capacity = capacity;
        this.type = type;
        this.buffer = (T[]) Array.newInstance(type, capacity);
        this.head = 0;
        this.tail = 0;
        this.size = 0;
    }

    public void add(T item) {
        buffer[tail] = item;
        tail = (tail + 1) % capacity;
        if (size < capacity) {
            size++;
        } else {
            head = (head + 1) % capacity;
        }
    }

    @SuppressWarnings("unchecked")
    public T[] getAll() {
        T[] result = (T[]) Array.newInstance(type, size);
        for (int i = 0; i < size; i++) {
            result[i] = buffer[(head + size - 1 - i) % capacity];
        }
        return result;
    }

    public int getSize() {
        return size;
    }
}