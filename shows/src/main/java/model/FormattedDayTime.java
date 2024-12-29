package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class FormattedDayTime {

    private static final String FORMAT = "MM/dd HH:mm";
    private final LocalDateTime dateTime;

    public FormattedDayTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String toString() {
        return this.dateTime.getDayOfWeek().name().substring(0, 1).toUpperCase()
                + this.dateTime.getDayOfWeek().name().substring(1).toLowerCase()
                + " "
                + this.dateTime.format(DateTimeFormatter.ofPattern(FORMAT));
    }
}