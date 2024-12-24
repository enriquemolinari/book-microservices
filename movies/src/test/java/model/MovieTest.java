package model;

import api.Genre;
import api.MoviesException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class MovieTest {

    private final ForTests tests = new ForTests();

    @Test
    public void smallFishMovie() {
        var smallFish = tests.createSmallFishMovie();

        assertTrue(smallFish.hasDurationOf(102));
        assertFalse(smallFish.hasDurationOf(10));
        assertTrue(smallFish.isNamed("Small Fish"));
        assertTrue(smallFish.isCharacterNamed("George Bix"));
        assertTrue(smallFish.hasReleaseDateOf(LocalDate.of(2023, 10, 10)));
        assertTrue(smallFish.hasGenresOf(List.of(Genre.COMEDY, Genre.ACTION)));
        assertTrue(smallFish.hasARole("aName aSurname"));
        assertTrue(smallFish.isDirectedBy("aDirectorName aDirectorSurname"));
    }

    @Test
    public void movieNameIsInvalid() {
        Exception e = assertThrows(MoviesException.class, () -> {
            new Movie("  ", "plot ...", 102,
                    LocalDate.of(2023, 10, 10) /* release data */,
                    Set.of(Genre.COMEDY, Genre.ACTION)/* genre */,
                    List.of(new Actor(
                            new Person("aName", "aSurname", "anEmail@mail.com"),
                            "George Bix")),
                    List.of(new Person("aDName", "aDSurname",
                            "anotherEmail@mail.com")));
        });
        assertEquals(Movie.MOVIE_NAME_INVALID, e.getMessage());
    }

    @Test
    public void durationIsInvalid() {
        Exception e = assertThrows(MoviesException.class, () -> {
            new Movie("Small Fish", "plot...", 0,
                    LocalDate.of(2023, 10, 10) /* release data */,
                    Set.of(Genre.COMEDY, Genre.ACTION)/* genre */,
                    List.of(new Actor(
                            new Person("aName", "aSurname", "anEmail@mail.com"),
                            "George Bix")),
                    List.of(new Person("aDName", "aDSurname",
                            "anotherEmail@mail.com")));
        });
        assertEquals(Movie.DURATION_INVALID, e.getMessage());
    }

    @Test
    public void genreIsInvalid() {
        Exception e = assertThrows(MoviesException.class, () -> {
            new Movie("Small Fish", "plot...", 100,
                    LocalDate.of(2023, 10, 10) /* release data */,
                    Set.of()/* genre */,
                    List.of(new Actor(
                            new Person("aName", "aSurname", "anEmail@mail.com"),
                            "George Bix")),
                    List.of(new Person("aDName", "aDSurname",
                            "anotherEmail@mail.com")));
        });
        assertEquals(Movie.GENRES_INVALID, e.getMessage());
    }

    @Test
    public void directorsWithBlankNames() {
        Exception e = assertThrows(MoviesException.class, () -> {
            new Movie("Small Fish", "plot...", 100,
                    LocalDate.of(2023, 10, 10) /* release data */,
                    Set.of(Genre.ACTION)/* genre */,
                    List.of(new Actor(
                            new Person("aName", "aSurname", "anEmail@mail.com"),
                            "George Bix")),
                    List.of(new Person(" ", "aSurname",
                                    "anotherEmail@mail.com"),
                            new Person("aName", "aSurname",
                                    "anotherOtherEmail@mail.com")));
        });
        assertEquals(Person.NAME_MUST_NOT_BE_BLANK, e.getMessage());
    }

    @Test
    public void newCreatedMovieHasCeroRate() {
        var smallFish = tests.createSmallFishMovie();
        assertTrue(smallFish.hasRateValue(0));
    }

    @Test
    public void ratedOk() {
        var smallFish = tests.createSmallFishMovie();
        smallFish.rateBy(tests.createUserCharly(), 2, "not so great movie");
        smallFish.rateBy(tests.createUserJoseph(), 5, "great movie");
        smallFish.rateBy(tests.createUserNicolas(), 4, "fantastic movie");
        assertTrue(smallFish.hasRateValue(3.67f));
        assertTrue(smallFish.hasTotalVotes(3));
    }
}
