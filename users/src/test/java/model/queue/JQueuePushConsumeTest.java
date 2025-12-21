package model.queue;

import jakarta.persistence.EntityManagerFactory;
import main.EmfBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static model.UsersTest.*;
import static org.awaitility.Awaitility.await;

public class JQueuePushConsumeTest {
    static final int TIMEOUT = 4;
    private static EntityManagerFactory emf;

    @BeforeAll
    public static void setUp() {
        emf = new EmfBuilder(DB_USER, DB_PWD)
                .memory(CONN_STR)
                .withDropAndCreateDDL()
                .build();
    }

    @BeforeEach
    public void afterEach() {
        emf.getSchemaManager().truncate();
    }

    @Test
    public void pushedInJQueueMustRepublish() throws InterruptedException {
        var pushedData = pushSampleJob();

        var fakePublisher = new Publisher() {
            private String pushedData;

            public boolean pushed(String pushedData) {
                return this.pushedData != null &&
                        this.pushedData.equals(pushedData);
            }

            @Override
            public void startUp() {

            }

            @Override
            public void push(String data) {
                this.pushedData = data;
            }

            @Override
            public void shutdown() {

            }
        };

        var dbConnStr = new DbConnStr(CONN_STR, DB_USER, DB_PWD);
        var worker = new PushToBrokerFromJQueueWorker(dbConnStr, fakePublisher);
        try {
            worker.startUpSchedule();
            await()
                    .atMost(Duration.of(TIMEOUT, ChronoUnit.SECONDS))
                    .until(() -> fakePublisher.pushed(pushedData));
        } finally {
            worker.shutdown();
        }
    }

    private String pushSampleJob() {
        var em = emf.createEntityManager();
        var jqueue = new JQueueInTxtQueue(em);
        var pushedData = "hello world";
        jqueue.push(pushedData);
        em.close();
        return pushedData;
    }
}
