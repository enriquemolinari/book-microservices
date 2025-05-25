package model.queue;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class RabbitMQPublisher implements Publisher {
    public static final String CONTENT_TYPE = "application/json";
    private final RabbitConnStr rabbitConnStr;
    private final String exchangeName;
    private Connection connection;
    private Channel channel;

    public RabbitMQPublisher(RabbitConnStr rabbitConnStr, String exchangeName) {
        this.rabbitConnStr = rabbitConnStr;
        this.exchangeName = exchangeName;
    }

    @Override
    public void startUp() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(rabbitConnStr.host());
            factory.setUsername(rabbitConnStr.user());
            factory.setPassword(rabbitConnStr.password());
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();
            channel.exchangeDeclare(this.exchangeName, BuiltinExchangeType.FANOUT, true);
            channel.confirmSelect();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void push(String data) {
        try {
            byte[] body = data.getBytes(StandardCharsets.UTF_8);
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .contentType(CONTENT_TYPE)
                    .deliveryMode(2) // persistente
                    .build();
            channel.basicPublish(
                    this.exchangeName,           // exchange
                    "",                      // routing key (vacío si fanout)
                    props,
                    body
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void shutdown() {
        try {
            if (this.channel != null && this.connection != null) {
                this.channel.close();
                this.connection.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("rabbitmq connection and channel could not be closed", e);
        }
    }
}
