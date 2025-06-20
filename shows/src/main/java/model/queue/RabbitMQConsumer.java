package model.queue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import model.Buyer;
import model.EntityCreator;
import model.Movie;
import model.events.consume.NewMovieEvent;
import model.events.consume.NewUserEvent;

public class RabbitMQConsumer implements Consumer {
    public static final String ENCODING = "UTF-8";
    private final RabbitConnStr rabbitConnStr;
    private final EntityCreator creator;
    private final String queueNameForNewUsers;
    private final String queueNameForNewMovie;

    public RabbitMQConsumer(RabbitConnStr rabbitConnStr, EntityCreator creator, String queueNameForNewUser, String queueNameForNewMovie) {
        this.rabbitConnStr = rabbitConnStr;
        this.creator = creator;
        this.queueNameForNewUsers = queueNameForNewUser;
        this.queueNameForNewMovie = queueNameForNewMovie;
    }

    public void listenForNewUsers() {
        executeOnEachMessage((payload) -> {
            var newUserEvent = NewUserEvent.of(payload);
            this.creator.persist(new Buyer(newUserEvent.userId()));
        }, this.queueNameForNewUsers);
    }

    public void listenForNewMovies() {
        executeOnEachMessage((payload) -> {
            var newMovieEvent = NewMovieEvent.of(payload);
            this.creator.persist(new Movie(newMovieEvent.id()));
        }, this.queueNameForNewMovie);
    }

    private void executeOnEachMessage(java.util.function.Consumer<String> executeThis, String queueName) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval(10000);
        factory.setHost(this.rabbitConnStr.host());
        factory.setUsername(this.rabbitConnStr.user());
        factory.setPassword(this.rabbitConnStr.password());
        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            // here is waiting for new messages
            channel.basicConsume(queueName, false, (consumerTag, delivery) -> {
                try {
                    String eventPayload = new String(delivery.getBody(), ENCODING);
                    // do process
                    executeThis.accept(eventPayload);
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
