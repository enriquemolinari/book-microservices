package main;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceConfiguration;
import model.*;
import model.queue.JQueueTable;

public class EmfBuilder {
//    public static final String DB_USER = "app";
//    public static final String DB_PWD = "app";
//    public static final String IN_MEMORY_DB_URL = "jdbc:derby:memory:cinema;create=true";
//    public static final String CLIENT_DB_URL = "jdbc:derby://%s:%s/%s;create=true";


    public static final String DO_NOTHING_WITH_SCHEMA = "none";
    public static final String DROP_AND_CREATE_SCHEMA = "drop-and-create";
    public static final String HIBERNATE_SHOW_SQL = "hibernate.show_sql";
    public static final String HIBERNATE_FORMAT_SQL = "hibernate.format_sql";
    public static final String HIBERNATE_HIGHLIGHT_SQL = "hibernate.highlight_sql";
    private PersistenceConfiguration config;

    public EmfBuilder(String dbUser, String dbPwd) {
        config = new PersistenceConfiguration("moviesMsConfig")
                .managedClass(Actor.class)
                .managedClass(Person.class)
                .managedClass(Movie.class)
                .managedClass(User.class)
                .managedClass(Rating.class)
                .managedClass(UserRate.class)
                .managedClass(JQueueTable.class)
                .property(PersistenceConfiguration.JDBC_USER, dbUser)
                .property(PersistenceConfiguration.JDBC_PASSWORD, dbPwd)
                .property("hibernate.cache.region.factory_class", "jcache")
                .property("hibernate.cache.use_second_level_cache", "true")
                .property("hibernate.cache.use_query_cache", "true")
                .property("hibernate.javax.cache.missing_cache_strategy", "create")
                .property(PersistenceConfiguration.SCHEMAGEN_DATABASE_ACTION,
                        DO_NOTHING_WITH_SCHEMA);
    }

    public EmfBuilder memory(String connStr) {
        config.property(PersistenceConfiguration.JDBC_URL,
                connStr);
        return this;
    }

    public EmfBuilder debugQueries() {
        config.property(HIBERNATE_SHOW_SQL, true);
        config.property(HIBERNATE_FORMAT_SQL, true);
        config.property(HIBERNATE_HIGHLIGHT_SQL, true);
        return this;
    }

    public EmfBuilder clientAndServer(String connStr) {
        config.property(PersistenceConfiguration.JDBC_URL, connStr);
        return this;
    }

    public EmfBuilder withDropAndCreateDDL() {
        config.property(PersistenceConfiguration.SCHEMAGEN_DATABASE_ACTION,
                DROP_AND_CREATE_SCHEMA);
        return this;
    }

    public EntityManagerFactory build() {
        return config.createEntityManagerFactory();
    }
}


