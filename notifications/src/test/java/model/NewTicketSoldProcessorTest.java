package model;

import main.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class NewTicketSoldProcessorTest {
    public static final String ENV_VALUE = "default";
    public static final int PORT_TEST = 8087;
    public static final String SALES_ID = "7f1ffef0-f0d4-4099-a368-d5e76c652a8b";
    private Config config;
    private ClientAndServer mockServer;
    private ForTests forTests = new ForTests();

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
                .respond(response().withBody(forTests.jsonShowSale()));

        var saleInfoRequestor = new SaleInfoRequestor(forTests.getUrl(this.config.gatewayHost(), PORT_TEST, this.config.salesRequestPath()));
        String[] values = new String[3];
        var showSoldProcessor = new NewTicketSoldProcessor(
                (emailTo, subject, body) -> {
                    values[0] = emailTo;
                    values[1] = subject;
                    values[2] = body;
                },
                saleInfoRequestor);
        showSoldProcessor.process(SALES_ID);
        assertEquals("enrique.molinari@gmail.com", values[0]);
        assertEquals("You have new tickets!", values[1]);
        assertEquals(forTests.expectedShowSoldBody(), values[2]);
    }
}


