package web;

import api.MoviesSubSystem;
import jakarta.persistence.Persistence;
import main.SetUpSampleDb;
import model.Movies;
import model.PersistenceUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class AppTestConfiguration {
    @Bean
    public MoviesSubSystem createMovies() {
        var emf = Persistence
                .createEntityManagerFactory(PersistenceUnit.DERBY_EMBEDDED_MOVIES_MS);
        new SetUpSampleDb(emf).createSchemaAndPopulateSampleData();
        return new Movies(emf);
    }
}
