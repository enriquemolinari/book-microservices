package api;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface MoviesSubSystem {

    List<MovieInfo> pagedMoviesSortedByName(int pageNumber);

    List<MovieInfo> pagedMoviesSortedByRate(int pageNumber);

    List<MovieInfo> pagedMoviesSortedByReleaseDate(int pageNumber);

    MovieInfo movie(Long id);

    MovieInfo addNewMovie(String name, int duration,
                          LocalDate releaseDate, String plot, Set<Genre> genres);

    MovieInfo addActorTo(Long movieId, String name, String surname,
                         String email, String characterName);

    MovieInfo addDirectorToMovie(Long movieId, String name,
                                 String surname, String email);

    UserMovieRate rateMovieBy(Long userId, Long idMovie, int rateValue,
                              String comment);

    List<UserMovieRate> pagedRatesOfOrderedDate(Long movieId, int pageNumber);

    List<MovieInfo> pagedSearchMovieByName(String fullOrPartmovieName,
                                           int pageNumber);
}
