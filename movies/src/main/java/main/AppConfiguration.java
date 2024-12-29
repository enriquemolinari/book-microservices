package main;

import api.MoviesSubSystem;
import jakarta.persistence.Persistence;
import model.Movies;
import model.PersistenceUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("default")
public class AppConfiguration {
    @Bean
    public MoviesSubSystem createMovies() {
        var emf = Persistence
                .createEntityManagerFactory(PersistenceUnit.DERBY_EMBEDDED_MOVIES_MS);
        new SetUpSampleDb(emf).createSchemaAndPopulateSampleData();
        return new Movies(emf);
    }
}