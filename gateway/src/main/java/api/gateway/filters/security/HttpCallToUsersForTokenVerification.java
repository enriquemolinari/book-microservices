package api.gateway.filters.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static java.net.http.HttpClient.newHttpClient;

@Component
@Primary
class HttpCallToUsersForTokenVerification implements TokenVerification {
    private final int timeoutInSeconds;
    private final String usersServerPort;
    private final String usersUriTokenVerificationPath;

    public HttpCallToUsersForTokenVerification(@Value("${users.serverPort}") String usersServerPort,
                                               @Value("${users.uriTokenVerificationPath}") String usersUriTokenVerificationPath,
                                               @Value("${httpclient.timeout}") int timeoutInSeconds) {
        this.usersServerPort = usersServerPort;
        this.usersUriTokenVerificationPath = usersUriTokenVerificationPath;
        this.timeoutInSeconds = timeoutInSeconds;
    }

    @Override
    public TokenVerificationResult verify(String token) {
        try {
            HttpRequest req = buildHttpRequest(token);
            HttpResponse<String> response = performRequest(req);
            if (response.statusCode() == HttpStatusCode.OK.code()) {
                return TokenVerificationResult.success(response.body());
            }
            return TokenVerificationResult.failure(response.statusCode(), response.body());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            return TokenVerificationResult.failure(HttpStatusCode.INTERNAL_SERVER_ERROR.code(), e.getMessage());
        }
    }

    private HttpResponse<String> performRequest(HttpRequest req) throws IOException, InterruptedException {
        HttpResponse<String> response;
        try (HttpClient httpClient = newHttpClient()) {
            response = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        }
        return response;
    }

    private HttpRequest buildHttpRequest(String token) throws URISyntaxException {
        String url = usersServerPort + usersUriTokenVerificationPath;
        HttpRequest req = HttpRequest.newBuilder(new URI(url))
                .POST(HttpRequest.BodyPublishers.ofString(token))
                .timeout(Duration.of(timeoutInSeconds, ChronoUnit.SECONDS))
                .build();
        return req;
    }
}
