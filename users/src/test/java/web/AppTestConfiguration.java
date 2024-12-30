package web;

import api.UsersSubSystem;
import jakarta.persistence.Persistence;
import main.SetUpSampleDb;
import model.PasetoToken;
import model.PersistenceUnit;
import model.Users;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class AppTestConfiguration {
    private static final String SECRET = "nXXh3Xjr2T0ofFilg3kw8BwDEyHmS6OIe4cjWUm2Sm0=";

    @Bean
    public UsersSubSystem createUsers() {
        var emf = Persistence
                .createEntityManagerFactory(PersistenceUnit.DERBY_EMBEDDED_USERS_MS);
        new SetUpSampleDb(emf).createSchemaAndPopulateSampleData();
        return new Users(emf, new PasetoToken(SECRET));

    }
}
