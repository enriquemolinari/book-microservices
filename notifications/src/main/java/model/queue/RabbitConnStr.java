package model.queue;

public record RabbitConnStr(String host, int port, String user, String password,
                            String queueName) {

}
