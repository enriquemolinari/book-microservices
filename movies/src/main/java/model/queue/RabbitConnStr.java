package model.queue;

public record RabbitConnStr(String host, Integer port, String user,
                            String password) {
}
