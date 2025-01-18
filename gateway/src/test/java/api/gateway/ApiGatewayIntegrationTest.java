package api.gateway;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest(classes = ApiGatewayApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles(value = "test")
public class ApiGatewayIntegrationTest {
    public static final String METHOD_POST = "POST";
    public static final String FAKE_TOKEN = "v2.token.fake";
    public static final String VERIFY_TOKEN_URI_PATH = "/users/token";
    public static final String FAKE_USER_ID = "114";
    public static final String USERS_PROFILE_URI_PATH = "/users/private/profile";
    public static final String INVALID_TOKEN_MSG = "INVALID TOKEN";
    public static final String MOVIES_RATE_URI_PATH = "/movies/private/1/rate";
    public static final String USERS_ANY_PUBLIC_URI_PATH = "/users/any";
    public static final String SOME_ANY_OK_MESSAGE = "some any ok message";
    public static final String MOVIES_PUBLIC_URI_PATH = "/movies";
    public static final String SHOWS_ANY_PUBLIC_URI = "/shows/any";
    public static final String SHOWS_PRIVATE_ANY = "/shows/private/any";
    private static final String NO_TOKEN_MSG = "Authentication is required";
    ClientAndServer usersMockServer;
    @Value("${port.users}")
    private int USERS_SERVER_PORT;
    @Value("${port.movies}")
    private int MOVIES_SERVER_PORT;
    @Value("${port.shows}")
    private int SHOWS_SERVER_PORT;
    @Value("${forward.requestHeaderUserId}")
    private String REQUEST_HEADER_KEY_USER_ID;
    @Value("${users.tokenCookieParamName}")
    private String TOKEN_COOKIE_PARAM_NAME;
    @Autowired
    private WebTestClient testClient;
    private ClientAndServer moviesMockServer;
    private ClientAndServer showsMockServer;

    @BeforeEach
    public void startMockServer() {
        usersMockServer = ClientAndServer.startClientAndServer(USERS_SERVER_PORT);
        moviesMockServer = ClientAndServer.startClientAndServer(MOVIES_SERVER_PORT);
        showsMockServer = ClientAndServer.startClientAndServer(SHOWS_SERVER_PORT);
    }

    @AfterEach
    public void stopMockServer() {
        usersMockServer.stop();
        moviesMockServer.stop();
        showsMockServer.stop();
    }

    @Test
    public void privateShowsPathWithValidTokenOk() {
        mockTokenValidationEndPointRespondOk();
        mockPrivateEndPointWithPath(showsMockServer, SHOWS_PRIVATE_ANY);
        testClient.get()
                .uri(SHOWS_PRIVATE_ANY)
                .cookie(TOKEN_COOKIE_PARAM_NAME, FAKE_TOKEN)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class).isEqualTo(SOME_ANY_OK_MESSAGE);
    }

    @Test
    public void privateUsersPathWithValidTokenOk() {
        mockTokenValidationEndPointRespondOk();
        mockPrivateEndPointWithPath(usersMockServer, USERS_PROFILE_URI_PATH);
        testClient.get()
                .uri(USERS_PROFILE_URI_PATH)
                .cookie(TOKEN_COOKIE_PARAM_NAME, FAKE_TOKEN)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class).isEqualTo(SOME_ANY_OK_MESSAGE);
    }

    @Test
    public void privateMoviesPathWithValidTokenOk() {
        mockTokenValidationEndPointRespondOk();
        mockPrivateEndPointWithPath(moviesMockServer, MOVIES_RATE_URI_PATH);
        testClient.post()
                .uri(MOVIES_RATE_URI_PATH)
                .cookie(TOKEN_COOKIE_PARAM_NAME, FAKE_TOKEN)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class).isEqualTo(SOME_ANY_OK_MESSAGE);
    }

    @Test
    public void publicShowsEndpointsAreForwardedOk() {
        mockPublicEndPointWithPath(showsMockServer, SHOWS_ANY_PUBLIC_URI);
        testClient.post()
                .uri(SHOWS_ANY_PUBLIC_URI)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo(SOME_ANY_OK_MESSAGE);
    }

    @Test
    public void publicUsersEndpointsAreForwardedOk() {
        mockPublicEndPointWithPath(usersMockServer, USERS_ANY_PUBLIC_URI_PATH);
        testClient.post()
                .uri(USERS_ANY_PUBLIC_URI_PATH)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo(SOME_ANY_OK_MESSAGE);
    }

    @Test
    public void publicMoviesEndpointsAreForwardedOk() {
        mockPublicEndPointWithPath(moviesMockServer, MOVIES_PUBLIC_URI_PATH);
        testClient.get()
                .uri(MOVIES_PUBLIC_URI_PATH)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo(SOME_ANY_OK_MESSAGE);
    }

    @Test
    public void privateEndpointsPathWithInvalidToken() {
        mockTokenValidationEndPointRespondInvalid();
        testClient.get()
                .uri(USERS_PROFILE_URI_PATH)
                .cookie(TOKEN_COOKIE_PARAM_NAME, FAKE_TOKEN)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(String.class)
                .isEqualTo("{\"message\": \"" + INVALID_TOKEN_MSG + "\"}");
    }

    @Test
    public void privateUriPathPathWithoutToken() {
        testClient.get()
                .uri(USERS_PROFILE_URI_PATH)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(String.class)
                .isEqualTo("{\"message\": \"" + NO_TOKEN_MSG + "\"}");
    }

    private void mockPrivateEndPointWithPath(ClientAndServer mockServerUsed
            , String privateEndPointUriPath) {
        mockServerUsed.when(request()
                        .withPath(privateEndPointUriPath)
                        .withHeader(REQUEST_HEADER_KEY_USER_ID, FAKE_USER_ID))
                .respond(response().withBody(SOME_ANY_OK_MESSAGE));
    }

    private void mockPublicEndPointWithPath(ClientAndServer mockServerUsed
            , String publicEndPointUriPath) {
        mockServerUsed.when(request()
                        .withPath(publicEndPointUriPath))
                .respond(response().withBody(SOME_ANY_OK_MESSAGE));
    }


    private void mockTokenValidationEndPointRespondOk() {
        usersMockServer.when(request()
                        .withMethod(METHOD_POST)
                        .withBody(FAKE_TOKEN)
                        .withPath(VERIFY_TOKEN_URI_PATH))
                .respond(response().withBody(FAKE_USER_ID));
    }

    private void mockTokenValidationEndPointRespondInvalid() {
        usersMockServer.when(request()
                        .withMethod(METHOD_POST)
                        .withBody(FAKE_TOKEN)
                        .withPath(VERIFY_TOKEN_URI_PATH))
                .respond(response().withStatusCode(401).withBody(INVALID_TOKEN_MSG));
    }
}
