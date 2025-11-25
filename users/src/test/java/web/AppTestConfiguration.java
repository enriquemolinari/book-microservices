package web;

import api.UsersSubSystem;
import main.EmfBuilder;
import main.SetUpSampleDb;
import model.PasetoToken;
import model.Users;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class AppTestConfiguration {
    private static final String SECRET = "nXXh3Xjr2T0ofFilg3kw8BwDEyHmS6OIe4cjWUm2Sm0=";
    @Value("${db.url}")
    private String dbUrl;
    @Value("${db.user}")
    private String dbUser;
    @Value("${db.pwd}")
    private String dbPassword;

    @Bean
    public UsersSubSystem createUsers() {
        var emf = new EmfBuilder(dbUser, dbPassword)
                .memory(dbUrl)
                .withDropAndCreateDDL()
                .build();
        new SetUpSampleDb(emf).createSchemaAndPopulateSampleData();
        return new Users(emf, new PasetoToken(SECRET));

    }
}
