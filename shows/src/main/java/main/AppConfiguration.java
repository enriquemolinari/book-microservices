package main;

import api.ShowsSubSystem;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.Persistence;
import model.CreditCardPaymentProvider;
import model.PersistenceUnit;
import model.Shows;
import model.queue.DbConnStr;
import model.queue.PushToBrokerFromJQueueWorker;
import model.queue.RabbitConnStr;
import model.queue.RabbitMQBroker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static model.PersistenceUnit.DERBY_EMBEDDED_SHOWS_MS;

@Configuration
@Profile("default")
public class AppConfiguration {
    @Value("${queue.rabbimq.exchange.name}")
    private String EXCHANGE_NAME;
    @Value("${queue.rabbitmq.host}")
    private String RABBITHOST;
    @Value("${queue.rabbitmq.username}")
    private String RABBIUSER;
    @Value("${queue.rabbitmq.password}")
    private String RABBITPWD;
    @Value("${db.url}")
    private String dbUrl;
    @Value("${db.user}")
    private String dbUser;
    @Value("${db.pwd}")
    private String dbPassword;

    private PushToBrokerFromJQueueWorker pushToBrokerFromJQueueWorker;

    @Bean
    public ShowsSubSystem createShows() {
        var emf = Persistence.
                createEntityManagerFactory(DERBY_EMBEDDED_SHOWS_MS,
                        PersistenceUnit.connStrProperties(dbUrl, dbUser, dbPassword));
        new SetUpSampleDb(emf).createSchemaAndPopulateSampleData();
        pushToBrokerFromJQueueWorker = new PushToBrokerFromJQueueWorker(
                new DbConnStr(dbUrl, dbUser, dbPassword),
                new RabbitMQBroker(new RabbitConnStr(RABBITHOST, RABBIUSER, RABBITPWD, EXCHANGE_NAME)));
        pushToBrokerFromJQueueWorker.startUpSchedule();
        return new Shows(emf, doNothingPaymentProvider());
    }

    private CreditCardPaymentProvider doNothingPaymentProvider() {
        return (creditCardNumber, expire, securityCode, totalAmount) -> {
        };
    }

    @PreDestroy
    public void shutdownQueue() {
        pushToBrokerFromJQueueWorker.shutdown();
    }
}