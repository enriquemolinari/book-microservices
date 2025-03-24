package model.events;

public record NewMovieEvent(String type, Long id) implements ToJson {
    public static final String NEW_MOVIE_EVENT = "NewMovieEvent";

    public NewMovieEvent(Long id) {
        this(NEW_MOVIE_EVENT, id);
    }
}
