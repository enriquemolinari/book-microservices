package api;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface MoviesSubSystem {

    List<DetailedMovieInfo> pagedMoviesSortedByName(int pageNumber);

    List<DetailedMovieInfo> pagedMoviesSortedByRate(int pageNumber);

    List<DetailedMovieInfo> pagedMoviesSortedByReleaseDate(int pageNumber);

    DetailedMovieInfo movie(Long id);

    List<MovieInfo> allMovieInfosBy(List<Long> ids);

    DetailedMovieInfo addNewMovie(String name, int duration,
                                  LocalDate releaseDate, String plot, Set<Genre> genres);

    DetailedMovieInfo addActorTo(Long movieId, String name, String surname,
                                 String email, String characterName);

    DetailedMovieInfo addDirectorToMovie(Long movieId, String name,
                                         String surname, String email);

    UserMovieRate rateMovieBy(Long userId, Long idMovie, int rateValue,
                              String comment);

    List<UserMovieRate> pagedRatesOfOrderedDate(Long movieId, int pageNumber);

    List<DetailedMovieInfo> pagedSearchMovieByName(String fullOrPartmovieName,
                                                   int pageNumber);
}
