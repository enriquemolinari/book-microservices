package model.queue;

public interface Publisher {
    void startUp();

    void push(String data);

    void shutdown();
}
