package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Set;

public record SalesInfo(String movieName, String username,
                        String email, Float total,
                        Set<Integer> seats, String showStartTime) {
    public static SalesInfo of(String body) {
        Gson gson = new GsonBuilder()
                .create();
        return gson.fromJson(body, SalesInfo.class);
    }
}
