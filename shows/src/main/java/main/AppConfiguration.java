package main;

import api.ShowsSubSystem;
import jakarta.persistence.Persistence;
import model.CreditCardPaymentProvider;
import model.PersistenceUnit;
import model.Shows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("default")
public class AppConfiguration {
    @Value("${movies.info.api.url}")
    private String moviesEndpoint;

    //TODO: read from properties to pass to objects to consume movies endpoint
    @Bean
    public ShowsSubSystem createShows() {
        var emf = Persistence
                .createEntityManagerFactory(PersistenceUnit.DERBY_EMBEDDED_SHOWS_MS);
        new SetUpSampleDb(emf).createSchemaAndPopulateSampleData();
        return new Shows(emf, doNothingPaymentProvider());
    }

    private CreditCardPaymentProvider doNothingPaymentProvider() {
        return (creditCardNumber, expire, securityCode, totalAmount) -> {
        };
    }
}