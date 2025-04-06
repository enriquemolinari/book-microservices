package model.queue;

import ar.cpfw.jqueue.runner.JQueueRunner;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static model.queue.JQueueTable.FULL_TABLE_NAME;

public class PushToBrokerFromJQueueWorker {
    final Logger logger = LoggerFactory.getLogger(PushToBrokerFromJQueueWorker.class);
    private final DbConnStr dbConnStr;
    private final EntityManagerFactory emf;
    private final Broker broker;

    public PushToBrokerFromJQueueWorker(EntityManagerFactory emf,
                                        DbConnStr dbConnStr,
                                        Broker broker) {
        this.emf = emf;
        this.dbConnStr = dbConnStr;
        this.broker = broker;
    }

    public void startUp() {
        try {
            this.broker.startUp();
            try (ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1)) {
                scheduler.scheduleAtFixedRate(() -> {
                    try (var em = this.emf.createEntityManager()) {
                        Session session = em.unwrap(Session.class);
                        session.doWork((c) -> executeJQueueRunner(this.broker));
                    }
                }, 0, 5, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void executeJQueueRunner(Broker broker) {
        logger.info("executing jqueue runner");
        JQueueRunner.runner(dbConnStr.url(), dbConnStr.user(), dbConnStr.password(), FULL_TABLE_NAME)
                .executeAll(data -> {
                    logger.info("pushing into rabbitmq: {}", data);
                    broker.push(data);
                });
    }

    public void shutdown() {
        this.broker.shutdown();
    }
}
