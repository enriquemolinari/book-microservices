package model;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class MailPitEmailProviderTest {

    private static final int SMTP_PORT = 1025;

    @Container
    private static final GenericContainer<?> mailpit = new GenericContainer<>("axllent/mailpit")
            .withExposedPorts(SMTP_PORT)
            .withEnv("MP_SMTP_AUTH_ACCEPT_ANY", "1")
            .withEnv("MP_SMTP_AUTH_ALLOW_INSECURE", "1");

    @Test
    void sendEmailSuccessfully() {
        String host = mailpit.getHost();
        Integer port = mailpit.getMappedPort(SMTP_PORT);

        MailPitEmailProvider emailProvider = new MailPitEmailProvider(
                host,
                port.toString(),
                "test@example.com"
        );

        assertDoesNotThrow(() -> emailProvider.send(
                "recipient@example.com",
                "Test Subject",
                "This is a test email body"
        ));
    }

    @Test
    void sendEmailFailsWithInvalidPort() {
        String host = mailpit.getHost();
        String invalidPort = "9999";

        MailPitEmailProvider emailProvider = new MailPitEmailProvider(
                host,
                invalidPort,
                "test@example.com"
        );

        var e = assertThrows(RuntimeException.class, () -> emailProvider.send(
                "recipient@example.com",
                "Test Subject",
                "This is a test email body"
        ));

        assertEquals(MailPitEmailProvider.THE_EMAIL_COULD_NOT_BE_SENT, e.getMessage());
    }
}

