package model.events.publish;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public record NewTicketsSoldEvent(String type, String saleId) {
    public static final String NEW_TICKET_SOLD = "NewTicketSold";

    public NewTicketsSoldEvent(String saleId) {
        this(NEW_TICKET_SOLD, saleId);
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
