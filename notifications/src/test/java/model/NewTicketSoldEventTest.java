package model;

import model.events.NewTicketSoldEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NewTicketSoldEventTest {
    @Test
    public void ticketSoldEventIsCreatedOk() {
        var ticketSoldEvent = NewTicketSoldEvent.of("{\"type\":\"NewTicketSold\",\"saleId\":\"5c5c163d-c6d1-4037-a8fd-1793da487bd7\"}");
        assertEquals("NewTicketSold", ticketSoldEvent.type());
        assertEquals("5c5c163d-c6d1-4037-a8fd-1793da487bd7", ticketSoldEvent.saleId());
    }
}
