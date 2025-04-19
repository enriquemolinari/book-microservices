package model;

import main.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class ShowSoldProcessorTest {
    public static final String ENV_VALUE = "default";
    public static final int PORT_TEST = 8087;
    public static final String SALES_ID = "7f1ffef0-f0d4-4099-a368-d5e76c652a8b";
    private Config config;
    private ClientAndServer mockServer;

    @BeforeEach
    public void startMockServer() {
        config = new Config(ENV_VALUE);
        mockServer = ClientAndServer
                .startClientAndServer(PORT_TEST);
    }

    @AfterEach
    public void stopMockServer() {
        mockServer.stop();
    }

    @Test
    public void emailServiceIsCalledOk() {
        mockServer.when(request().
                        withPath(config
                                .salesRequestPath()
                                .formatted(SALES_ID)))
                .respond(response().withBody(jsonShowSale()));

        var saleInfoRequestor = new SaleInfoRequestor(getUrl());
        String[] values = new String[3];
        var showSoldProcessor = new ShowSoldProcessor(
                (emailTo, subject, body) -> {
                    values[0] = emailTo;
                    values[1] = subject;
                    values[2] = body;
                },
                saleInfoRequestor);
        showSoldProcessor.process(SALES_ID);
        assertEquals("enrique.molinari@gmail.com", values[0]);
        assertEquals("You have new tickets!", values[1]);
        assertEquals(expectedBody(), values[2]);
    }

    private String getUrl() {
        return "http://" + config.gatewayHost() + ":" + PORT_TEST + config.salesRequestPath();
    }

    private String expectedBody() {
        return """
                Hello emolinari!
                You have new tickets!
                Here are the details of your booking:
                Movie: The Movie of the Century
                Seats: 3
                Show time: Tuesday 04/08 20:56
                Total paid: 10.0""";
    }

    private String jsonShowSale() {
        return
                """                        
                        {
                                         "salesIdentifier": "7f1ffef0-f0d4-4099-a368-d5e76c652a8b",
                                         "movieId": 2,
                                         "movieName": "The Movie of the Century",
                                         "userId": 2,
                                         "username": "emolinari",
                                         "fullname": "Enrique Molinari",
                                         "email": "enrique.molinari@gmail.com",
                                         "total": 10.0,
                                         "pointsWon": 10,
                                         "seats": [
                                            3
                                         ],
                                         "showStartTime": "Tuesday 04/08 20:56"
                                         }
                        """;
    }
}

