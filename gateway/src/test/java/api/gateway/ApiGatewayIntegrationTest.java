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
    public static final String USERS_PROFILE_URI_PATH = "/users/profile";
    public static final String FAKE_PROFILE_DATA = "{SOME PROFILE}";
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
    public void test01() {
        mockTokenValidationEndPointValid();

        mockServer.when(request()
                        .withPath(USERS_PROFILE_URI_PATH)
                        .withHeader(REQUEST_HEADER_KEY_USER_ID, FAKE_USER_ID))
                .respond(response().withBody(FAKE_PROFILE_DATA));

        testClient.get()
                .uri(USERS_PROFILE_URI_PATH)
                .cookie(TOKEN_COOKIE_PARAM_NAME, FAKE_TOKEN)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class).isEqualTo(FAKE_PROFILE_DATA);
    }

    private void mockTokenValidationEndPointValid() {
        mockServer.when(request()
                        .withMethod(METHOD_POST)
                        .withBody(FAKE_TOKEN)
                        .withPath(VERIFY_TOKEN_URI_PATH))
                .respond(response().withBody(FAKE_USER_ID));
    }
    //TODO: token present invalid
//    status 401{
//        "message": "Invalid token. You have to login."
//    }
    //TODO: token present valid
    //TODO: token not present

}
