package model;

import api.ActorInMovieName;
import api.Genre;
import api.MovieInfo;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class ForTests {

    static final String SUPER_MOVIE_PLOT = "a super movie that shows the life of ...";
    static final String SUPER_MOVIE_NAME = "a super movie";
    static final String OTHER_SUPER_MOVIE_NAME = "another super movie";
    static final String SUPER_MOVIE_DIRECTOR_NAME = "aDirectorName surname";
    static final ActorInMovieName SUPER_MOVIE_ACTOR_CARLOS = new ActorInMovieName(
            "Carlos Kalchi",
            "aCharacterName");


    Movie createSmallFishMovie() {
        return createSmallFishMovie(LocalDate.of(2023, 10, 10));
    }

    Movie createSmallFishMovie(LocalDate releaseDate) {
        return new Movie("Small Fish", "plot x", 102,
                releaseDate,
                Set.of(Genre.COMEDY, Genre.ACTION)/* genre */,
                List.of(new Actor(
                        new Person("aName", "aSurname", "anEmail@mail.com"),
                        "George Bix")),
                List.of(new Person("aDirectorName", "aDirectorSurname",
                        "anotherEmail@mail.com")));
    }


    User createUserCharly() {
        return new User(1L, "cedgun");
    }

    User createUserJoseph() {
        return new User(2L, "jvaldun");
    }

    User createUserNicolas() {
        return new User(3L, "nmolinari");
    }

    MovieInfo createSuperMovie(Movies cinema) {
        var movieInfo = cinema.addNewMovie(SUPER_MOVIE_NAME, 109,
                LocalDate.of(2023, 4, 5),
                SUPER_MOVIE_PLOT,
                Set.of(Genre.ACTION, Genre.ADVENTURE));

        cinema.addActorTo(movieInfo.id(), "Carlos", "Kalchi",
                "carlosk@bla.com", "aCharacterName");

        cinema.addActorTo(movieInfo.id(), "Jose", "Hermes",
                "jose@bla.com", "anotherCharacterName");

        cinema.addDirectorToMovie(movieInfo.id(), "aDirectorName", "surname",
                "adir@bla.com");

        return movieInfo;
    }

    MovieInfo createOtherSuperMovie(Movies cinema) {
        var movieInfo = cinema.addNewMovie(OTHER_SUPER_MOVIE_NAME, 80,
                LocalDate.of(2022, 4, 5),
                "other super movie ...",
                Set.of(Genre.COMEDY, Genre.FANTASY));

        cinema.addActorTo(movieInfo.id(), "Nico", "Cochix",
                "nico@bla.com", "super Character Name");

        cinema.addDirectorToMovie(movieInfo.id(), "aSuper DirectorName",
                "sur name",
                "asuper@bla.com");

        return movieInfo;
    }

}