package api;

import java.util.List;
import java.util.Set;

public record MovieShows(
        Long movieId,
        String movieName,
        String duration,
        Set<String> genres,
        List<ShowInfo> shows
) {
}
