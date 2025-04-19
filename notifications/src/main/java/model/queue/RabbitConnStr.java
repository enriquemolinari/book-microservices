package model.queue;

public record RabbitConnStr(String host, String user, String password,
                            String queueName) {

}
