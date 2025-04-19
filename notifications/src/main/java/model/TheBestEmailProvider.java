package model;

public class TheBestEmailProvider implements EmailProvider {

    @Override
    public void send(String to, String subject, String body) {
        // implement with your favourite email provider
        System.out.printf("""
                Email sent successfully to %s with subject: '%s' and body: '%s'%n""", to, subject, body);
    }
}
