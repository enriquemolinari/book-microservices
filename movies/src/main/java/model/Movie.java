package model;

import api.*;
import common.FormattedDate;
import common.NotBlankString;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static model.Schema.DATABASE_SCHEMA_NAME;

@Entity
@Table(schema = DATABASE_SCHEMA_NAME)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
public class Movie {
    static final String MOVIE_PLOT_INVALID = "Movie plot must not be null or blank";
    static final String MOVIE_NAME_INVALID = "Movie name must not be null or blank";
    static final String DURATION_INVALID = "Movie's duration must be greater than 0";
    static final String GENRES_INVALID = "You must add at least one genre to the movie";
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;
    private int duration;
    private LocalDate releaseDate;
    private String plot;
    @ElementCollection(targetClass = Genre.class)
    @CollectionTable(schema = DATABASE_SCHEMA_NAME)
    @Enumerated(EnumType.STRING)
    private Set<Genre> genres;
    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "id_movie")
    private List<Actor> actors;
    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(schema = DATABASE_SCHEMA_NAME)
    private List<Person> directors;
    @OneToMany(cascade = CascadeType.PERSIST, mappedBy = "movie")
    // List does not load the entire collection for adding new elements
    // if there is a bidirectional mapping
    private List<UserRate> userRates;
    // this is pre-calculated rating for this movie
    @Embedded
    private Rating rating;

    public Movie(String name, String plot, int duration, LocalDate releaseDate,
                 Set<Genre> genres, List<Actor> actors, List<Person> directors) {
        checkDurationGreaterThanZero(duration);
        checkGenresAtLeastHasOne(genres);
        this.name = new NotBlankString(name, new MoviesException(MOVIE_NAME_INVALID)).value();
        this.plot = new NotBlankString(plot, new MoviesException(MOVIE_PLOT_INVALID)).value();
        this.duration = duration;
        this.releaseDate = releaseDate;
        this.genres = genres;
        this.actors = actors;
        this.directors = directors;
        this.userRates = new ArrayList<>();
        this.rating = Rating.notRatedYet();
    }

    public Movie(String name, String plot, int duration, LocalDate releaseDate,
                 Set<Genre> genres) {
        this(name, plot, duration, releaseDate, genres, new ArrayList<>(),
                new ArrayList<>());
    }

    private void checkGenresAtLeastHasOne(Set<Genre> genres) {
        if (genres.isEmpty()) {
            throw new MoviesException(GENRES_INVALID);
        }
    }

    private void checkDurationGreaterThanZero(int duration) {
        if (duration <= 0) {
            throw new MoviesException(DURATION_INVALID);
        }
    }

    public boolean hasDurationOf(int aDuration) {
        return this.duration == aDuration;
    }

    public boolean isNamed(String aName) {
        return this.name.equals(aName);
    }

    public boolean hasReleaseDateOf(LocalDate aDate) {
        return releaseDate.equals(aDate);
    }

    public boolean hasGenresOf(List<Genre> genddres) {
        return genddres.containsAll(this.genres);
    }

    public boolean hasARole(String anActorName) {
        return this.actors.stream().anyMatch(a -> a.isNamed(anActorName));
    }

    public boolean isCharacterNamed(String aCharacterName) {
        return this.actors.stream()
                .anyMatch(a -> a.hasCharacterName(aCharacterName));
    }

    public boolean isDirectedBy(String aDirectorName) {
        return this.directors.stream().anyMatch(d -> d.isNamed(aDirectorName));
    }

    public UserRate rateBy(User user, int value, String comment) {
        // Ideally validating logic that a user does not rate the same
        // movie twice should be here. However, to do that Hibernate will
        // load the entire collection in memory. That
        // would hurt performance as the collection gets bigger.
        // This validation gets performed in Cimema.
        var userRate = new UserRate(user, value, comment, this);
        this.rating.calculaNewRate(value);
        this.userRates.add(userRate);
        return userRate;
    }

    boolean hasRateValue(float aValue) {
        return this.rating.hasValue(aValue);
    }

    public boolean hasTotalVotes(int votes) {
        return this.rating.hastTotalVotesOf(votes);
    }

    public void addAnActor(String name, String surname, String email,
                           String characterName) {
        this.actors.add(
                new Actor(new Person(name, surname, email), characterName));
    }

    public void addADirector(String name, String surname, String email) {
        this.directors.add(new Person(name, surname, email));
    }

    public DetailedMovieInfo toDetailedInfo() {
        return new DetailedMovieInfo(id, name,
                new MovieDurationFormat(duration).toString(), plot,
                genreAsListOfString(), directorsNamesAsString(),
                new FormattedDate(releaseDate).toString(),
                rating.actualRateAsString(), rating.totalVotes(),
                toActorsInMovieNames());
    }

    private List<String> directorsNamesAsString() {
        return directors.stream().map(Person::fullName).toList();
    }

    private List<ActorInMovieName> toActorsInMovieNames() {
        return this.actors.stream()
                .map(actor -> new ActorInMovieName(actor.fullName(),
                        actor.characterName()))
                .toList();
    }

    Set<String> genreAsListOfString() {
        return this.genres.stream().map(g -> capitalizeFirstLetter(g.name()))
                .collect(Collectors.toSet());
    }

    private String capitalizeFirstLetter(String aString) {
        return aString.substring(0, 1).toUpperCase()
                + aString.substring(1).toLowerCase();
    }

    public MovieInfo toInfo() {
        return new MovieInfo(id, name,
                new MovieDurationFormat(duration).toString(),
                genreAsListOfString());
    }

    public void name(String newName) {
        this.name = newName;
    }
}
