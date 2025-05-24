package main;

import api.UsersSubSystem;
import jakarta.persistence.Persistence;
import model.PasetoToken;
import model.PersistenceUnit;
import model.Users;
import model.queue.DbConnStr;
import model.queue.PushToBrokerFromJQueueWorker;
import model.queue.RabbitConnStr;
import model.queue.RabbitMQPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("default")
public class AppConfiguration {
    //this must be outside repository
    private static final String SECRET = "nXXh3Xjr2T0ofFilg3kw8BwDEyHmS6OIe4cjWUm2Sm0=";
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
    public UsersSubSystem createUsers() {
        var emf = Persistence
                .createEntityManagerFactory(PersistenceUnit.DERBY_EMBEDDED_USERS_MS);
        new SetUpSampleDb(emf).createSchemaAndPopulateSampleData();
        pushToBrokerFromJQueueWorker = new PushToBrokerFromJQueueWorker(
                new DbConnStr(dbUrl, dbUser, dbPassword),
                new RabbitMQPublisher(new RabbitConnStr(RABBITHOST, RABBIUSER, RABBITPWD, EXCHANGE_NAME)));
        pushToBrokerFromJQueueWorker.startUp();
        return new Users(emf, new PasetoToken(SECRET));
    }
}