package model;

import java.util.Map;

public interface PersistenceUnit {
    String DERBY_CLIENT_SHOWS_MS = "derby-client-shows-ms";
    String DERBY_EMBEDDED_SHOWS_MS = "derby-inmemory-shows-ms";
    String JDBC_DERBY_MEMORY_SHOWS = "jdbc:derby:memory:shows;create=true";
    String USER = "app";
    String PWD = "app";

    static Map<String, String> connStrInMemoryProperties() {
        return Map.of("jakarta.persistence.jdbc.url", JDBC_DERBY_MEMORY_SHOWS,
                "jakarta.persistence.jdbc.user", USER,
                "jakarta.persistence.jdbc.password", PWD);
    }

    static Map<String, String> connStrInClientProperties() {
        return Map.of("jakarta.persistence.jdbc.url", JDBC_DERBY_MEMORY_SHOWS,
                "jakarta.persistence.jdbc.user", USER,
                "jakarta.persistence.jdbc.password", PWD);
    }
}
