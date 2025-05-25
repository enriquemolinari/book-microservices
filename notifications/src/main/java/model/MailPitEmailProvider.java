package model;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class MailPitEmailProvider implements EmailProvider {

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
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "false"); // sin TLS

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("noauth", "noauth");
            }
        });
        try {
            // Crear mensaje
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
        } catch (Exception e) {
            throw new RuntimeException("The email could not be sent", e);
        }
    }
}
