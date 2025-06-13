package model.queue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import model.NewTicketSoldProcessor;
import model.events.NewTicketSoldEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitMQConsumer implements Consumer {
    public static final String ENCODING = "UTF-8";
    final Logger logger = LoggerFactory.getLogger(RabbitMQConsumer.class);
    private final RabbitConnStr rabbitConnStr;
    private final NewTicketSoldProcessor processor;

    public RabbitMQConsumer(RabbitConnStr rabbitConnStr, NewTicketSoldProcessor processor) {
        this.rabbitConnStr = rabbitConnStr;
        this.processor = processor;
    }

    public void listenForNewTickets() {
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
                    // d|o process
                    var ticketSoldEvent = NewTicketSoldEvent.of(eventPayload);
                    this.processor.process(ticketSoldEvent.saleId());
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(),
                            false /* not muliple, confirm this eventPayload only */);
                } catch (Exception e) {
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(),
                            false /* not multiple, just this one */,
                            false /* not requeue, move to dlq */);
                    logger.error("Consuming from {} failed", this.rabbitConnStr.queueName(), e);
                }
            }, consumerTag -> {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
