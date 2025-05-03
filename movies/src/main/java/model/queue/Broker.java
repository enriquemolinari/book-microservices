package model.queue;

public interface Broker {
    void startUp();

    void push(String data);

    void shutdown();
}
