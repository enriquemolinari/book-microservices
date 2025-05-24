package main;

import api.MoviesSubSystem;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.Persistence;
import model.Movies;
import model.PersistenceUnit;
import model.UserCreator;
import model.queue.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("default")
public class AppConfiguration {
    @Value("${queue.rabbimq.exchange.name}")
    private String EXCHANGE_NAME;
    @Value("${queue.rabbimq.newuser.queue.name}")
    private String QUEUE_NAME;
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
    public MoviesSubSystem createMovies() {
        var emf = Persistence
                .createEntityManagerFactory(PersistenceUnit.DERBY_EMBEDDED_MOVIES_MS,
                        PersistenceUnit.connStrProperties(dbUrl, dbUser, dbPassword));
        new SetUpSampleDb(emf).createSchemaAndPopulateSampleData();
        pushToBrokerFromJQueueWorker = new PushToBrokerFromJQueueWorker(
                new DbConnStr(dbUrl, dbUser, dbPassword),
                new RabbitMQPublisher(new RabbitConnStr(RABBITHOST, RABBIUSER, RABBITPWD), EXCHANGE_NAME));
        pushToBrokerFromJQueueWorker.startUp();

        new RabbitMQConsumer(
                new RabbitConnStr(RABBITHOST, RABBIUSER, RABBITPWD),
                new UserCreator(emf),
                QUEUE_NAME).listenForNewUsers();
        return new Movies(emf);
    }

    @PreDestroy
    public void shutdownQueue() {
        pushToBrokerFromJQueueWorker.shutdown();
    }
}