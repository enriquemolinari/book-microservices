package model.events;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonStringToEvent<T> {
    private final Class<T> type;

    public JsonStringToEvent(Class<T> type) {
        this.type = type;
    }

    public T fromJson(String json) {
        Gson gson = new GsonBuilder()
                .create();
        return gson.fromJson(json, type);
    }
}
