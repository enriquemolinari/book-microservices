package model.events;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public record NewTicketSoldEvent(String type, String saleId) {
    public static NewTicketSoldEvent of(String event) {
        Gson gson = new GsonBuilder()
                .create();
        return gson.fromJson(event, NewTicketSoldEvent.class);
    }
}
