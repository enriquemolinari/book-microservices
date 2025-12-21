package model.queue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitConsumerTest {
    public static final String ENCODING = "UTF-8";
    private final RabbitConnStr rabbitConnStr;
    private final String queueName;
    private String payloadReceived;

    public RabbitConsumerTest(RabbitConnStr rabbitConnStr, String queueName) {
        this.rabbitConnStr = rabbitConnStr;
        this.queueName = queueName;
    }

    public void listenForNewUsers() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval(10000);
        factory.setHost(this.rabbitConnStr.host());
        factory.setPort(this.rabbitConnStr.port());
        factory.setUsername(this.rabbitConnStr.user());
        factory.setPassword(this.rabbitConnStr.password());
        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            // here is waiting for new messages
            channel.basicConsume(this.queueName, false, (consumerTag, delivery) -> {
                try {
                    // do process
                    this.payloadReceived = new String(delivery.getBody(), ENCODING);
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

    public String payloadReceived() {
        return payloadReceived;
    }
}