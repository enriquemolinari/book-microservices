package web;

import api.ShowsSubSystem;
import jakarta.persistence.Persistence;
import main.SetUpSampleDb;
import model.CreditCardPaymentProvider;
import model.PersistenceUnit;
import model.Shows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class AppTestConfiguration {
    @Value("${db.url}")
    private String dbUrl;
    @Value("${db.user}")
    private String dbUser;
    @Value("${db.pwd}")
    private String dbPassword;

    @Bean
    public ShowsSubSystem createShows() {
        var emf = Persistence
                .createEntityManagerFactory(PersistenceUnit.DERBY_EMBEDDED_SHOWS_MS,
                        PersistenceUnit.connStrProperties(dbUrl, dbUser, dbPassword));
        new SetUpSampleDb(emf).createSchemaAndPopulateSampleData();
        return new Shows(emf, doNothingPaymentProvider());
    }

    private CreditCardPaymentProvider doNothingPaymentProvider() {
        return (creditCardNumber, expire, securityCode, totalAmount) -> {
        };
    }

}
