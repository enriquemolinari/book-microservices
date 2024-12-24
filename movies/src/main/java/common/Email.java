package common;

public class Email {

    public static final String NOT_VALID_EMAIL = "Email address is not valid";
    private static final String REGEX = "^[\\w-_.+]*[\\w-_.]@(\\w+\\.)+\\w+\\w$";
    private final String email;

    public Email(String email) {
        if (!email.matches(REGEX)) {
            throw new RuntimeException(NOT_VALID_EMAIL);
        }

        this.email = email;
    }

    public String asString() {
        return email;
    }

}
