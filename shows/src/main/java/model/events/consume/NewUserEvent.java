package model.events.consume;

import model.events.JsonStringToEvent;

public record NewUserEvent(String type, long userId) {
    public static NewUserEvent of(String event) {
        return new JsonStringToEvent<>(NewUserEvent.class).fromJson(event);
    }
}

