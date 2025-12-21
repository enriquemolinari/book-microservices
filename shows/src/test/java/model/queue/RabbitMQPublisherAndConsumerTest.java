package model.queue;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import jakarta.persistence.EntityManagerFactory;
import main.EmfBuilder;
import model.Buyer;
import model.EntityCreator;
import model.Movie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static model.ShowsTest.*;
import static org.awaitility.Awaitility.await;

@Testcontainers
public class RabbitMQPublisherAndConsumerTest {

    static final String RABBITMQ_4_0_7 = "rabbitmq:4.0.7-management";

    @Container
    public static final RabbitMQContainer rabbit = new RabbitMQContainer(RABBITMQ_4_0_7);

    static final String EXCHANGE_USERS = "users.events";
    static final String EXCHANGE_MOVIES = "movies.events";
    static final String QUEUE_NAME_NEWUSER = "shows.users.events";
    static final String QUEUE_NAME_NEWMOVIE = "shows.movies.events";
    static final String DLQ_NEWUSER = "dlq.shows.users.events";
    static final String DLQ_NEWMOVIE = "dlq.shows.movies.events";
    private static final long AWAIT_TIMEOUT_SECONDS = 4;
    private static EntityManagerFactory emf;
    private RabbitMQPublisher userPublisher;
    private RabbitMQPublisher moviePublisher;
    private Connection setupConnection;
    private Channel setupChannel;

    @BeforeAll
    public static void setUpDb() {
        emf = new EmfBuilder(DB_USER, DB_PWD)
                .memory(CONN_STR)
                .withDropAndCreateDDL()
                .build();
    }

    @BeforeEach
    public void setUp() throws Exception {

        // Setup RabbitMQ infrastructure
        setupRabbitMQInfrastructure();

        // Setup publishers
        RabbitConnStr conn = createRabbitConn();

        userPublisher = new RabbitMQPublisher(conn, EXCHANGE_USERS);
        userPublisher.startUp();

        moviePublisher = new RabbitMQPublisher(conn, EXCHANGE_MOVIES);
        moviePublisher.startUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (userPublisher != null) {
            userPublisher.shutdown();
        }
        if (moviePublisher != null) {
            moviePublisher.shutdown();
        }
        if (setupChannel != null) {
            setupChannel.close();
        }
        if (setupConnection != null) {
            setupConnection.close();
        }
    }

    @Test
    public void listenForNewMoviesShouldConsumeMessageAndPersistMovie() throws Exception {
        startListenNewMoviesPushAndConsume((anyStr) -> {
        });
    }

    @Test
    public void listenForNewMoviesHandleIdempotency() throws Exception {
        startListenNewMoviesPushAndConsume((movieEventPayload) -> moviePublisher.push(movieEventPayload));
    }

    @Test
    public void listenForNewUsersShouldConsumeMessageAndPersistBuyer() throws Exception {
        startListenNewUsersPushAndConsume((anyStr) -> {
        });
    }

    @Test
    public void listenForNewUsersHandleIdempotency() throws Exception {
        startListenNewUsersPushAndConsume((userEventPayload) -> userPublisher.push(userEventPayload));
    }

    private void startListenNewUsersPushAndConsume(Consumer<String> morePushs) throws InterruptedException {
        var entityCreator = new EntityCreator(emf);
        RabbitConnStr conn = createRabbitConn();
        RabbitMQConsumer consumer = createRabbitConsumer(conn, entityCreator);

        // Start consumer in a separate thread
        startListenerThread(() -> consumer.listenForNewUsers());

        // Give consumer time to start listening
        Thread.sleep(1000);

        // Act - Publish a NewUserEvent
        long expectedUserId = 123L;
        String userEventPayload = String.format("{\"type\":\"NewUser\",\"userId\":%d}", expectedUserId);
        userPublisher.push(userEventPayload);
        morePushs.accept(userEventPayload);

        await()
                .atMost(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .until(() -> entityCreator.exists(expectedUserId, new Buyer(expectedUserId)));
    }

    private void startListenNewMoviesPushAndConsume(Consumer<String> morePushs) throws InterruptedException {
        var entityCreator = new EntityCreator(emf);
        RabbitConnStr conn = createRabbitConn();
        RabbitMQConsumer consumer = createRabbitConsumer(conn, entityCreator);

        // Start consumer in a separate thread
        startListenerThread(() -> consumer.listenForNewMovies());

        // Give consumer time to start listening
        Thread.sleep(1000);

        // Act - Publish a NewMovieEvent
        long expectedMovieId = 456L;
        String movieEventPayload = String.format("{\"type\":\"NewMovie\",\"id\":%d}", expectedMovieId);
        moviePublisher.push(movieEventPayload);
        morePushs.accept(movieEventPayload);

        await()
                .atMost(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .until(() -> entityCreator.exists(expectedMovieId, new Movie(expectedMovieId)));
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
        setupChannel.exchangeDeclare(EXCHANGE_MOVIES, BuiltinExchangeType.FANOUT, true);

        // Declare DLQs
        setupChannel.queueDeclare(DLQ_NEWUSER, true, false, false, null);
        setupChannel.queueDeclare(DLQ_NEWMOVIE, true, false, false, null);

        // Declare main queues with DLQ configuration
        java.util.Map<String, Object> userQueueArgs = new java.util.HashMap<>();
        userQueueArgs.put("x-dead-letter-exchange", "");
        userQueueArgs.put("x-dead-letter-routing-key", DLQ_NEWUSER);
        setupChannel.queueDeclare(QUEUE_NAME_NEWUSER, true, false, false, userQueueArgs);

        java.util.Map<String, Object> movieQueueArgs = new java.util.HashMap<>();
        movieQueueArgs.put("x-dead-letter-exchange", "");
        movieQueueArgs.put("x-dead-letter-routing-key", DLQ_NEWMOVIE);
        setupChannel.queueDeclare(QUEUE_NAME_NEWMOVIE, true, false, false, movieQueueArgs);

        // Bind queues to exchanges
        setupChannel.queueBind(QUEUE_NAME_NEWUSER, EXCHANGE_USERS, "");
        setupChannel.queueBind(QUEUE_NAME_NEWMOVIE, EXCHANGE_MOVIES, "");

        // Purge queues to ensure clean state
        setupChannel.queuePurge(QUEUE_NAME_NEWUSER);
        setupChannel.queuePurge(QUEUE_NAME_NEWMOVIE);
    }

    private void startListenerThread(Runnable runnable) {
        Thread consumerThread = new Thread(runnable);
        consumerThread.setDaemon(true);
        consumerThread.start();
    }

    private RabbitMQConsumer createRabbitConsumer(RabbitConnStr conn, EntityCreator testEntityCreator) {
        return new RabbitMQConsumer(
                conn,
                testEntityCreator,
                QUEUE_NAME_NEWUSER,
                QUEUE_NAME_NEWMOVIE
        );
    }

    private RabbitConnStr createRabbitConn() {
        return new RabbitConnStr(
                rabbit.getHost(),
                rabbit.getAmqpPort(),
                rabbit.getAdminUsername(),
                rabbit.getAdminPassword()
        );
    }
}