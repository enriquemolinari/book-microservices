package model.events.consume;

import model.events.JsonStringToEvent;

public record NewMovieEvent(String type, long id) {
    public static NewMovieEvent of(String event) {
        return new JsonStringToEvent<>(NewMovieEvent.class).fromJson(event);
    }
}
