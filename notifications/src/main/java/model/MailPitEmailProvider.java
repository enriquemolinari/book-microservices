package model;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class MailPitEmailProvider implements EmailProvider {

    static final String THE_EMAIL_COULD_NOT_BE_SENT = "The email could not be sent";
    static final String MAIL_SMTP_HOST = "mail.smtp.host";
    static final String MAIL_SMTP_PORT = "mail.smtp.port";
    static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
    private final String host;
    private final String port;
    private final String mailFrom;

    public MailPitEmailProvider(String host, String port, String mailFrom) {
        this.host = host;
        this.port = port;
        this.mailFrom = mailFrom;
    }

    @Override
    public void send(String to, String subject, String body) {
        Properties props = new Properties();
        props.put(MAIL_SMTP_HOST, host);
        props.put(MAIL_SMTP_PORT, port);
        props.put(MAIL_SMTP_AUTH, "true");
        props.put(MAIL_SMTP_STARTTLS_ENABLE, "false"); // sin TLS

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("noauth", "noauth");
            }
        });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
        } catch (Exception e) {
            throw new RuntimeException(THE_EMAIL_COULD_NOT_BE_SENT, e);
        }
    }
}
