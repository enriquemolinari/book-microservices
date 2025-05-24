package model.events.consume;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public record NewUserEvent(String type, long userId) {
    public static NewUserEvent of(String event) {
        Gson gson = new GsonBuilder()
                .create();
        return gson.fromJson(event, NewUserEvent.class);
    }
}
