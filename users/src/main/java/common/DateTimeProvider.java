package common;

import java.time.LocalDateTime;

@FunctionalInterface
public interface DateTimeProvider {

    static DateTimeProvider create() {
        return LocalDateTime::now;
    }

    LocalDateTime now();
}
