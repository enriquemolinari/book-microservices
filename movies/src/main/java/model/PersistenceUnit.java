package model;

import java.util.Map;

public interface PersistenceUnit {
    String DERBY_CLIENT_MOVIES_MS = "derby-client-movies-ms";
    String DERBY_EMBEDDED_MOVIES_MS = "derby-inmemory-movies-ms";

    static Map<String, String> connStrProperties(String url, String user, String pwd) {
        return Map.of("jakarta.persistence.jdbc.url", url,
                "jakarta.persistence.jdbc.user", user,
                "jakarta.persistence.jdbc.password", pwd);
    }
}
