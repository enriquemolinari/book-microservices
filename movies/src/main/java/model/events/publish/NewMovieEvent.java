package model.events.publish;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public record NewMovieEvent(String type, Long id) {
    public static final String NEW_MOVIE_EVENT = "NewMovieEvent";

    public NewMovieEvent(Long id) {
        this(NEW_MOVIE_EVENT, id);
    }

    public String toJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
