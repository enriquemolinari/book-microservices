package web;

import api.MoviesSubSystem;
import builder.SetUpDb;
import jakarta.persistence.Persistence;
import model.Movies;
import model.PersistenceUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {
    @Bean
    public MoviesSubSystem createMovies() {
        var emf = Persistence
                .createEntityManagerFactory(PersistenceUnit.DERBY_CLIENT_MOVIES_MS);
        new SetUpDb(emf).createSchemaAndPopulateSampleData();
        return new Movies(emf);
    }
}