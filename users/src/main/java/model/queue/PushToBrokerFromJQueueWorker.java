package model.queue;

import ar.cpfw.jqueue.runner.JQueueRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static model.queue.JQueueTable.FULL_TABLE_NAME;

public class PushToBrokerFromJQueueWorker {
    final Logger logger = LoggerFactory.getLogger(PushToBrokerFromJQueueWorker.class);
    private final DbConnStr dbConnStr;
    private final Publisher publisher;
    private ScheduledExecutorService scheduler;

    public PushToBrokerFromJQueueWorker(DbConnStr dbConnStr,
                                        Publisher publisher) {
        this.dbConnStr = dbConnStr;
        this.publisher = publisher;
    }

    public void startUp() {
        try {
            this.publisher.startUp();
            scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                executeJQueueRunner(this.publisher);
            }, 0, 5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void executeJQueueRunner(Publisher publisher) {
        logger.info("executing jqueue runner");
        JQueueRunner.runner(dbConnStr.url(), dbConnStr.user(), dbConnStr.password(), FULL_TABLE_NAME)
                .executeAll(data -> {
                    logger.info("pushing into rabbitmq: {}", data);
                    publisher.push(data);
                });
    }

    public void shutdown() {
        this.scheduler.close();
        this.publisher.shutdown();
    }
}
