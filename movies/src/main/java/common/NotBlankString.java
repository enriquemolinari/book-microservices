package common;


public class NotBlankString {

    private final String value;

    public NotBlankString(String value, RuntimeException e) {
        if (value == null || value.isBlank()) {
            throw e;
        }
        this.value = value;
    }

    public String value() {
        return value;
    }
}
