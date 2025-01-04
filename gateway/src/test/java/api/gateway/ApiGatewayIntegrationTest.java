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
// Note: This test requires all services up and running
@ActiveProfiles(value = "test")
public class ApiGatewayIntegrationTest {
    public static final String METHOD_POST = "POST";
    public static final String FAKE_TOKEN = "v2.token.fake";
    public static final String VERIFY_TOKEN_URI_PATH = "/users/token";
    public static final String FAKE_USER_ID = "114";
    public static final String USERS_PROFILE_URI_PATH = "/users/private/profile";
    public static final String FAKE_PROFILE_DATA = "{SOME PROFILE}";
    public static final String INVALID_TOKEN_MSG = "INVALID TOKEN";
    private static final String NO_TOKEN_MSG = "Authentication is required";
    ClientAndServer mockServer;
    @Value("${port.users}")
    private int USERS_SERVER_PORT;
    @Value("${server.port}")
    private String SERVER_PORT;
    @Value("${forward.requestHeaderUserId}")
    private String REQUEST_HEADER_KEY_USER_ID;
    @Value("${users.tokenCookieParamName}")
    private String TOKEN_COOKIE_PARAM_NAME;
    @Autowired
    private WebTestClient testClient;

    @BeforeEach
    public void startMockServer() {
        mockServer = ClientAndServer.startClientAndServer(USERS_SERVER_PORT);
    }

    @AfterEach
    public void stopMockServer() {
        mockServer.stop();
    }

    @Test
    public void privateUsersPathWithValidTokenOk() {
        mockTokenValidationEndPointRespondOk();
        mockPrivateEndPointWithPath(USERS_PROFILE_URI_PATH);
        testClient.get()
                .uri(USERS_PROFILE_URI_PATH)
                .cookie(TOKEN_COOKIE_PARAM_NAME, FAKE_TOKEN)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class).isEqualTo(FAKE_PROFILE_DATA);
    }

    @Test
    public void privateUsersPathWithInvalidToken() {
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

    private void mockPrivateEndPointWithPath(String privateEndPointUriPath) {
        mockServer.when(request()
                        .withPath(privateEndPointUriPath)
                        .withHeader(REQUEST_HEADER_KEY_USER_ID, FAKE_USER_ID))
                .respond(response().withBody(FAKE_PROFILE_DATA));
    }

    private void mockTokenValidationEndPointRespondOk() {
        mockServer.when(request()
                        .withMethod(METHOD_POST)
                        .withBody(FAKE_TOKEN)
                        .withPath(VERIFY_TOKEN_URI_PATH))
                .respond(response().withBody(FAKE_USER_ID));
    }

    private void mockTokenValidationEndPointRespondInvalid() {
        mockServer.when(request()
                        .withMethod(METHOD_POST)
                        .withBody(FAKE_TOKEN)
                        .withPath(VERIFY_TOKEN_URI_PATH))
                .respond(response().withStatusCode(401).withBody(INVALID_TOKEN_MSG));
    }
}
