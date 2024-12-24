package common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FormattedDateTime {

    private static final String FORMAT = "MM-dd-yyyy HH:mm";
    private final LocalDateTime dateTime;

    public FormattedDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String toString() {
        return this.dateTime.format(DateTimeFormatter.ofPattern(FORMAT));
    }
}
