package model.queue;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import main.Config;
import model.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Testcontainers
public class RabbitMQPublisherAndConsumerTest {

    public static final String ENV_VALUE = "default";
    public static final int PORT_TEST = 8087;
    public static final String SALES_ID = "7f1ffef0-f0d4-4099-a368-d5e76c652a8b";
    static final String RABBITMQ_4_0_7 = "rabbitmq:4.0.7-management";
    @Container
    public static final RabbitMQContainer rabbit = new RabbitMQContainer(RABBITMQ_4_0_7);
    static final String EXCHANGE_NOTIF = "notifications.events";
    static final String QUEUE_NAME_NOTIF = "notifications.shows.events";
    static final String DLQ_NOTIF = "dlq.notifications.shows.events";
    static final String EMAIL = "enrique.molinari@gmail.com";
    private static final long AWAIT_TIMEOUT_SECONDS = 4;
    private RabbitMQPublisherTest testPublisher;
    private Connection setupConnection;
    private Channel setupChannel;
    private Config config;
    private ClientAndServer mockServer;
    private ForTests forTests = new ForTests();

    @BeforeEach
    public void setUp() throws Exception {
        config = new Config(ENV_VALUE);

        mockServer = ClientAndServer
                .startClientAndServer(PORT_TEST);

        // Setup RabbitMQ infrastructure
        setupRabbitMQInfrastructure();

        // Setup publishers
        RabbitConnStr conn = createRabbitConn();

        testPublisher = new RabbitMQPublisherTest(conn, EXCHANGE_NOTIF);
        testPublisher.startUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (testPublisher != null) {
            testPublisher.shutdown();
        }
        if (setupChannel != null) {
            setupChannel.close();
        }
        if (setupConnection != null) {
            setupConnection.close();
        }
        mockServer.stop();
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
        setupChannel.exchangeDeclare(EXCHANGE_NOTIF, BuiltinExchangeType.FANOUT, true);

        // Declare DLQs
        setupChannel.queueDeclare(DLQ_NOTIF, true, false, false, null);

        // Declare main queues with DLQ configuration
        java.util.Map<String, Object> userQueueArgs = new java.util.HashMap<>();
        userQueueArgs.put("x-dead-letter-exchange", "");
        userQueueArgs.put("x-dead-letter-routing-key", DLQ_NOTIF);
        setupChannel.queueDeclare(QUEUE_NAME_NOTIF, true, false, false, userQueueArgs);

        // Bind queues to exchanges
        setupChannel.queueBind(QUEUE_NAME_NOTIF, EXCHANGE_NOTIF, "");

        // Purge queues to ensure clean state
        setupChannel.queuePurge(QUEUE_NAME_NOTIF);
    }

    @Test
    public void consumeNotificationEventOk() throws InterruptedException {
        String[] emailValues = new String[3];
        var showSoldProcessor = new NewTicketSoldProcessor(
                getEmailProvider(emailValues),
                prepareSaleInfoRequestor());

        RabbitConnStr conn = createRabbitConn();
        RabbitMQConsumer consumer = createRabbitConsumer(conn, showSoldProcessor);
        // Start consumer in a separate thread
        startListenerThread(() -> consumer.listenForNewTickets());

        // Give consumer time to start listening
        Thread.sleep(1000);

        // Publish a NewTicketSoldEvent
        String newTicketSoldEvent = String.format("{\"type\":\"NewTicketSold\",\"saleId\":%s}", SALES_ID);
        testPublisher.push(newTicketSoldEvent);

        await()
                .atMost(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .until(() -> emailValues[0] != null
                        && emailValues[0].equals(EMAIL)
                        && emailValues[1].equals(NewSaleEmailTemplate.EMAIL_SUBJECT_SALE)
                        && emailValues[2].equals(forTests.expectedShowSoldBody()));
    }

    private EmailProvider getEmailProvider(String[] emailValues) {
        return (emailTo, subject, body) -> {
            emailValues[0] = emailTo;
            emailValues[1] = subject;
            emailValues[2] = body;
        };
    }

    private @NotNull SaleInfoRequestor prepareSaleInfoRequestor() {
        mockServer.when(request().
                        withPath(config
                                .salesRequestPath()
                                .formatted(SALES_ID)))
                .respond(response().withBody(forTests.jsonShowSale()));

        var saleInfoRequestor = new SaleInfoRequestor(forTests.getUrl(this.config.gatewayHost(), PORT_TEST, this.config.salesRequestPath()));
        return saleInfoRequestor;
    }

    private void startListenerThread(Runnable runnable) {
        Thread consumerThread = new Thread(runnable);
        consumerThread.setDaemon(true);
        consumerThread.start();
    }

    private RabbitMQConsumer createRabbitConsumer(RabbitConnStr conn, NewTicketSoldProcessor processor) {
        return new RabbitMQConsumer(
                conn,
                processor
        );
    }

    private RabbitConnStr createRabbitConn() {
        return new RabbitConnStr(
                rabbit.getHost(),
                rabbit.getAmqpPort(),
                rabbit.getAdminUsername(),
                rabbit.getAdminPassword(),
                QUEUE_NAME_NOTIF
        );
    }
}