package model;

import java.util.Map;

public interface PersistenceUnit {
    String DERBY_CLIENT_SHOWS_MS = "derby-client-shows-ms";
    String DERBY_EMBEDDED_SHOWS_MS = "derby-inmemory-shows-ms";

    static Map<String, String> connStrProperties(String url, String user, String pwd) {
        return Map.of("jakarta.persistence.jdbc.url", url,
                "jakarta.persistence.jdbc.user", user,
                "jakarta.persistence.jdbc.password", pwd);
    }
}
