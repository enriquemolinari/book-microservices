package model;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static java.net.http.HttpClient.newHttpClient;

public class SaleInfoRequestor {
    private static final int TIMEOUT_SECONDS = 5;
    private final String url;


    public SaleInfoRequestor(String url) {
        this.url = url;
    }

    public SalesInfo makeRequest(String salesId) {
        try (HttpClient httpClient = newHttpClient()) {
            var req = HttpRequest.newBuilder(new URI(this.url.formatted(salesId)))
                    .GET()
                    .timeout(Duration.of(TIMEOUT_SECONDS, ChronoUnit.SECONDS))
                    .build();
            var responseBody = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return SalesInfo.of(responseBody.body());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
