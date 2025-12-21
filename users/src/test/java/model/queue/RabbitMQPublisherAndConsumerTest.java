package model.queue;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@Testcontainers
public class RabbitMQPublisherAndConsumerTest {

    static final String RABBITMQ_4_0_7 = "rabbitmq:4.0.7-management";

    @Container
    public static final RabbitMQContainer rabbit = new RabbitMQContainer(RABBITMQ_4_0_7);
    static final String EXCHANGE_USERS = "users.events";
    static final String QUEUE_NAME_NEWUSER = "users.events";
    static final String DLQ_NEWUSER = "dlq.users.events";
    private static final long AWAIT_TIMEOUT_SECONDS = 4;
    private RabbitMQPublisher userPublisher;
    private Connection setupConnection;
    private Channel setupChannel;

    @BeforeEach
    public void setUp() throws Exception {

        // Setup RabbitMQ infrastructure
        setupRabbitMQInfrastructure();

        // Setup publishers
        RabbitConnStr conn = createRabbitConn();

        userPublisher = new RabbitMQPublisher(conn);
        userPublisher.startUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (userPublisher != null) {
            userPublisher.shutdown();
        }
        if (setupChannel != null) {
            setupChannel.close();
        }
        if (setupConnection != null) {
            setupConnection.close();
        }
    }

    private void setupRabbitMQInfrastructure() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbit.getHost());
        factory.setPort(rabbit.getAmqpPort());
        factory.setUsername(rabbit.getAdminUsername());
        factory.setPassword(rabbit.getAdminPassword());

        setupConnection = factory.newConnection();
        setupChannel = setupConnection.createChannel();

        // Declare exchanges
        setupChannel.exchangeDeclare(EXCHANGE_USERS, BuiltinExchangeType.FANOUT, true);

        // Declare DLQs
        setupChannel.queueDeclare(DLQ_NEWUSER, true, false, false, null);

        // Declare main queues with DLQ configuration
        java.util.Map<String, Object> userQueueArgs = new java.util.HashMap<>();
        userQueueArgs.put("x-dead-letter-exchange", "");
        userQueueArgs.put("x-dead-letter-routing-key", DLQ_NEWUSER);
        setupChannel.queueDeclare(QUEUE_NAME_NEWUSER, true, false, false, userQueueArgs);

        // Bind queues to exchanges
        setupChannel.queueBind(QUEUE_NAME_NEWUSER, EXCHANGE_USERS, "");

        // Purge queues to ensure clean state
        setupChannel.queuePurge(QUEUE_NAME_NEWUSER);
    }

    @Test
    public void pushingANewUserEventOk() throws Exception {
        RabbitConnStr conn = createRabbitConn();
        var consumer = createRabbitConsumer(conn);

        // Start consumer in a separate thread
        startListenerThread(() -> consumer.listenForNewUsers());

        // Give consumer time to start listening
        Thread.sleep(1000);

        // Act - Publish a NewUserEvent
        long expectedUserId = 123L;
        String userEventPayload = String.format("{\"type\":\"NewUser\",\"userId\":%d}", expectedUserId);
        userPublisher.push(userEventPayload);

        await()
                .atMost(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .until(() -> consumer.payloadReceived().equals(userEventPayload));
    }

    private void startListenerThread(Runnable runnable) {
        Thread consumerThread = new Thread(runnable);
        consumerThread.setDaemon(true);
        consumerThread.start();
    }

    private RabbitConsumerTest createRabbitConsumer(RabbitConnStr conn) {
        return new RabbitConsumerTest(
                conn,
                QUEUE_NAME_NEWUSER
        );
    }

    private RabbitConnStr createRabbitConn() {
        return new RabbitConnStr(
                rabbit.getHost(),
                rabbit.getAmqpPort(),
                rabbit.getAdminUsername(),
                rabbit.getAdminPassword(),
                EXCHANGE_USERS
        );
    }
}
