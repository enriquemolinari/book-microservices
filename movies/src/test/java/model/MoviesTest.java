package model;

import api.DetailedMovieInfo;
import api.Genre;
import api.MovieInfo;
import api.MoviesException;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static model.ForTests.*;
import static model.PersistenceUnit.DERBY_EMBEDDED_MOVIES_MS;
import static org.junit.jupiter.api.Assertions.*;

public class MoviesTest {

    private static final Long NON_EXISTENT_ID = -2L;

    private final ForTests tests = new ForTests();
    private EntityManagerFactory emf;

    private List<String> genresEnumSetToUpperCaseStrings(Set<Genre> comedyFantasyGenres) {
        return comedyFantasyGenres.stream().map(s -> s.toString().toUpperCase())
                .toList();
    }

    private Movies createMoviesSubsystemWithPaging(int pageSize) {
        return new Movies(emf, pageSize);
    }

    private Movies createMoviesSubsystem() {
        return new Movies(emf);
    }

    @BeforeEach
    public void setUp() {
        emf = Persistence.createEntityManagerFactory(DERBY_EMBEDDED_MOVIES_MS);
    }

    @Test
    public void rateMovie() {
        var movies = createMoviesSubsystem();
        var movieInfo = tests.createSuperMovie(movies);
        var joseId = registerUserJose(movies);
        var userRate = movies.rateMovieBy(joseId, movieInfo.id(), 4,
                "great movie");
        assertEquals(joseId, userRate.userId());
        assertEquals(4, userRate.rateValue());
    }

    @Test
    public void retrieveRatesInvalidPageNumber() {
        var movies = createMoviesSubsystemWithPaging(10);
        var e = assertThrows(MoviesException.class, () -> {
            movies.pagedRatesOfOrderedDate(1L, 0);
        });
        assertEquals(Movies.PAGE_NUMBER_MUST_BE_GREATER_THAN_ZERO,
                e.getMessage());
    }

    @Test
    public void retrievePagedRatesFromMovie() {
        var movies = createMoviesSubsystemWithPaging(2);
        var movieInfo = tests.createSuperMovie(movies);
        var joseId = registerUserJose(movies);
        var userId = registerAUser(movies);
        var antonioId = registerUserAntonio(movies);
        movies.rateMovieBy(userId, movieInfo.id(), 1, "very bad movie");
        movies.rateMovieBy(joseId, movieInfo.id(), 2, "bad movie");
        movies.rateMovieBy(antonioId, movieInfo.id(), 3, "regular movie");
        var userRates = movies.pagedRatesOfOrderedDate(movieInfo.id(), 1);
        assertEquals(2, userRates.size());
        assertEquals(antonioId, userRates.get(0).userId());
        assertEquals(joseId, userRates.get(1).userId());
    }

    @Test
    public void retrieveAllPagedRates() {
        var movies = createMoviesSubsystemWithPaging(2);
        var superMovieInfo = tests.createSuperMovie(movies);
        var otherMovieInfo = tests.createOtherSuperMovie(movies);
        var joseId = registerUserJose(movies);
        movies.rateMovieBy(joseId, superMovieInfo.id(), 1, "very bad movie");
        movies.rateMovieBy(joseId, otherMovieInfo.id(), 3, "fine movie");
        var moviesList = movies.pagedMoviesSortedByRate(1);
        assertEquals(2, moviesList.size());
        assertEquals(ForTests.OTHER_SUPER_MOVIE_NAME, moviesList.get(0).name());
        assertEquals(ForTests.SUPER_MOVIE_NAME, moviesList.get(1).name());
    }

    @Test
    public void rateTheSameMovieTwice() {
        var movies = createMoviesSubsystem();
        var movieInfo = tests.createSuperMovie(movies);
        var joseId = registerUserJose(movies);
        movies.rateMovieBy(joseId, movieInfo.id(), 4, "great movie");
        var e = assertThrows(MoviesException.class, () -> {
            movies.rateMovieBy(joseId, movieInfo.id(), 4, "great movie");
            fail("I was able to rate the same movie twice");
        });
        assertEquals(Movies.USER_HAS_ALREADY_RATE, e.getMessage());
    }

    @Test
    public void retrieveMovie() {
        var movies = createMoviesSubsystem();
        var superMovie = tests.createSuperMovie(movies);
        DetailedMovieInfo movie = movies.movie(superMovie.id());
        assertEquals(2, movie.actors().size());
        assertEquals(1, movie.directorNames().size());
        assertEquals(SUPER_MOVIE_DIRECTOR_NAME, movie.directorNames().getFirst());
        assertTrue(movie.actors()
                .contains(SUPER_MOVIE_ACTOR_CARLOS));
        assertEquals(SUPER_MOVIE_NAME, movie.name());
        assertEquals(SUPER_MOVIE_PLOT, movie.plot());
    }

    @Test
    public void moviesSortedByReleaseDate() {
        var movies = createMoviesSubsystemWithPaging(1);
        tests.createSuperMovie(movies);
        tests.createOtherSuperMovie(movies);
        var moviesList = movies.pagedMoviesSortedByReleaseDate(1);
        assertEquals(1, moviesList.size());
        assertEquals(SUPER_MOVIE_NAME, moviesList.getFirst().name());
    }

    @Test
    public void retrieveAllMoviesInfo() {
        var movies = createMoviesSubsystem();
        var superMovie = tests.createSuperMovie(movies);
        var otherSuperMovie = tests.createOtherSuperMovie(movies);
        var otherMoreSuperMovie = tests.createOtherMoreSuperMovie(movies);
        var allMoviesInfo = movies.allMovieInfosBy(
                List.of(superMovie.id(), otherSuperMovie.id(), otherMoreSuperMovie.id()));
        assertEquals(3, allMoviesInfo.size());
        assertEquals(SUPER_MOVIE_NAME, allMoviesInfo.getFirst().name());
        assertEquals(new MovieDurationFormat(SUPER_MOVIE_DURATION).toString()
                , allMoviesInfo.get(0).duration());
        assertTrue(genresEnumSetToUpperCaseStrings(ACTION_ADVENTURE_GENRES)
                .containsAll(genresStringsSetToUpperCase(allMoviesInfo, 0)));
        assertEquals(OTHER_SUPER_MOVIE_NAME, allMoviesInfo.get(1).name());
        assertEquals(OTHER_MORE_SUPER_MOVIE_NAME, allMoviesInfo.get(2).name());
        assertEquals(new MovieDurationFormat(OTHER_SUPER_MOVIE_DURATION).toString()
                , allMoviesInfo.get(1).duration());
        assertEquals(new MovieDurationFormat(OTHER_MORE_SUPER_MOVIE_DURATION).toString()
                , allMoviesInfo.get(2).duration());
        assertTrue(genresEnumSetToUpperCaseStrings(COMEDY_FANTASY_GENRES)
                .containsAll(genresStringsSetToUpperCase(allMoviesInfo, 1)));
        assertTrue(genresEnumSetToUpperCaseStrings(COMEDY_FANTASY_GENRES)
                .containsAll(genresStringsSetToUpperCase(allMoviesInfo, 2)));
    }

    private List<String> genresStringsSetToUpperCase(List<MovieInfo> allMoviesInfo, int index) {
        return allMoviesInfo.get(index).genres().stream().map(String::toUpperCase)
                .toList();
    }

    @Test
    public void retrieveAllMovies() {
        var movies = createMoviesSubsystemWithPaging(1);
        tests.createSuperMovie(movies);
        tests.createOtherSuperMovie(movies);
        var moviesList = movies.pagedMoviesSortedByName(1);
        assertEquals(1, moviesList.size());
        assertEquals(SUPER_MOVIE_NAME, moviesList.getFirst().name());
        assertEquals(2, moviesList.getFirst().genres().size());
        assertEquals(2, moviesList.getFirst().actors().size());
        var moviesList2 = movies.pagedMoviesSortedByName(2);
        assertEquals(1, moviesList2.size());
        assertEquals(OTHER_SUPER_MOVIE_NAME, moviesList2.getFirst().name());
        assertEquals(2, moviesList2.getFirst().genres().size());
        assertEquals(1, moviesList2.getFirst().actors().size());
    }

    @Test
    public void searchMovieByName() {
        var movies = createMoviesSubsystemWithPaging(10);
        tests.createSuperMovie(movies);
        tests.createOtherSuperMovie(movies);
        var moviesList = movies.pagedSearchMovieByName("another", 1);
        assertEquals(1, moviesList.size());
        assertEquals(OTHER_SUPER_MOVIE_NAME, moviesList.getFirst().name());
    }

    @Test
    public void searchMovieByNameNotFound() {
        var movies = createMoviesSubsystemWithPaging(10);
        tests.createSuperMovie(movies);
        tests.createOtherSuperMovie(movies);
        var moviesList = movies.pagedSearchMovieByName("not_found_movie", 1);
        assertEquals(0, moviesList.size());
    }

    @Test
    public void movieIdNotExists() {
        var movies = createMoviesSubsystemWithPaging(10);
        var e = assertThrows(MoviesException.class, () -> {
            movies.movie(NON_EXISTENT_ID);
            fail("MovieId should not exists in the database");
        });
        assertEquals(Movies.MOVIE_ID_DOES_NOT_EXISTS, e.getMessage());
    }

    private Long registerUserJose(Movies movies) {
        return movies.addNewUser(1L);
    }

    private Long registerUserAntonio(Movies movies) {
        return movies.addNewUser(2L);
    }

    private Long registerAUser(Movies movies) {
        return movies.addNewUser(3L);
    }

    @AfterEach
    public void tearDown() {
        emf.close();
    }
}
