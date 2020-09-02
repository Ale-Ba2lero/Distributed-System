package client;

public class Pair<T> {
    private T key;
    private T value;

    public Pair() {}

    public Pair(T key, T value) {
        this.key = key;
        this.value = value;
    }

    public T getKey() {
        return this.key;
    }

    public T getValue() {
        return this.value;
    }

    public void setKey(T key) {
        this.key = key;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
