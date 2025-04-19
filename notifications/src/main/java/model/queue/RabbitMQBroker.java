package model.queue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import model.NewTicketSoldProcessor;
import model.events.NewTicketSoldEvent;

public class RabbitMQBroker implements Broker {
    public static final String ENCODING = "UTF-8";
    private final RabbitConnStr rabbitConnStr;
    private final NewTicketSoldProcessor processor;

    public RabbitMQBroker(RabbitConnStr rabbitConnStr, NewTicketSoldProcessor processor) {
        this.rabbitConnStr = rabbitConnStr;
        this.processor = processor;
    }

    public void listen() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.rabbitConnStr.host());
        factory.setUsername(this.rabbitConnStr.user());
        factory.setPassword(this.rabbitConnStr.password());
        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            // here is waiting for new messages
            channel.basicConsume(this.rabbitConnStr.queueName(), false, (consumerTag, delivery) -> {
                try {
                    String eventPayload = new String(delivery.getBody(), ENCODING);
                    // do process
                    var ticketSoldEvent = NewTicketSoldEvent.of(eventPayload);
                    this.processor.process(ticketSoldEvent.saleId());
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
