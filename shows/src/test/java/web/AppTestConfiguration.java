package web;

import api.ShowsSubSystem;
import jakarta.persistence.Persistence;
import main.SetUpSampleDb;
import model.CreditCardPaymentProvider;
import model.MoviesHttpSyncCallProvider;
import model.PersistenceUnit;
import model.Shows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class AppTestConfiguration {
    @Value("${movies.info.api.url}")
    private String mockServerPort;

    @Bean
    public ShowsSubSystem createShows() {
        var emf = Persistence
                .createEntityManagerFactory(PersistenceUnit.DERBY_EMBEDDED_SHOWS_MS);
        new SetUpSampleDb(emf).createSchemaAndPopulateSampleData();
        return new Shows(emf, doNothingPaymentProvider(), new MoviesHttpSyncCallProvider(mockServerPort));
    }

    private CreditCardPaymentProvider doNothingPaymentProvider() {
        return (creditCardNumber, expire, securityCode, totalAmount) -> {
        };
    }

}
