package model.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public record NewUserEvent(String type, Long userId) {
    public static final String NEW_USER_EVENT = "NewUserEvent";

    public NewUserEvent(Long userId) {
        this(NEW_USER_EVENT, userId);
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
