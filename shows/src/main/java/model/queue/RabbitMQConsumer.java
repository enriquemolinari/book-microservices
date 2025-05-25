package model.queue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import model.BuyerCreator;
import model.events.consume.NewUserEvent;

public class RabbitMQConsumer implements Consumer {
    public static final String ENCODING = "UTF-8";
    private final RabbitConnStr rabbitConnStr;
    private final BuyerCreator creator;
    private final String queueName;

    public RabbitMQConsumer(RabbitConnStr rabbitConnStr, BuyerCreator creator, String queueName) {
        this.rabbitConnStr = rabbitConnStr;
        this.creator = creator;
        this.queueName = queueName;
    }

    public void listenForNewUsers() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.rabbitConnStr.host());
        factory.setUsername(this.rabbitConnStr.user());
        factory.setPassword(this.rabbitConnStr.password());
        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            // here is waiting for new messages
            channel.basicConsume(this.queueName, false, (consumerTag, delivery) -> {
                try {
                    String eventPayload = new String(delivery.getBody(), ENCODING);
                    // do process
                    var newUserEvent = NewUserEvent.of(eventPayload);
                    this.creator.newUser(newUserEvent.userId());
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(),
                            false /* not muliple, confirm this eventPayload only */);
                } catch (Exception e) {
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(),
                            false /* not multiple, just this one */,
                            false /* not requeue, move to dlq */);
                }
            }, consumerTag -> {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
