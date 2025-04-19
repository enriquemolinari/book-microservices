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

import static model.PersistenceUnit.*;

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
    private PushToBrokerFromJQueueWorker pushToBrokerFromJQueueWorker;

    @Bean
    public ShowsSubSystem createShows() {
        var emf = Persistence.
                createEntityManagerFactory(DERBY_EMBEDDED_SHOWS_MS,
                        PersistenceUnit.connStrInMemoryProperties());
        new SetUpSampleDb(emf).createSchemaAndPopulateSampleData();
        pushToBrokerFromJQueueWorker = new PushToBrokerFromJQueueWorker(
                new DbConnStr(JDBC_DERBY_MEMORY_SHOWS, USER, PWD),
                new RabbitMQBroker(new RabbitConnStr(RABBITHOST, RABBIUSER, RABBITPWD, EXCHANGE_NAME)));
        pushToBrokerFromJQueueWorker.startUp();
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