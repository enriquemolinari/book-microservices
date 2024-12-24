package api;

import java.util.List;
import java.util.Set;

public record MovieInfo(Long id, String name, String duration, String plot,
		Set<String> genres, List<String> directorNames, String releaseDate, String ratingValue, int ratingTotalVotes,
		List<ActorInMovieName> actors) {
}
