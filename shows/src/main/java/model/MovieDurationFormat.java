package model;

import java.time.Duration;

class MovieDurationFormat {

    private final int duration;

    public MovieDurationFormat(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        var duration = Duration.ofMinutes(this.duration);
        var description = "%dhr";

        if (duration.toHours() > 1) {
            description += "s";
        }

        description += " %02dmin";

        if (duration.toMinutesPart() > 1) {
            description += "s";
        }

        return String.format(description, duration.toHours(),
                duration.toMinutesPart());
    }
}