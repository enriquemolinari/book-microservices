package api;

import java.util.List;

public record MovieShows(
        Long movieId,
        List<ShowInfo> shows
) {
}
