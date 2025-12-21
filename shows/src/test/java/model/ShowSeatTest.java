package model;

import api.ShowsException;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ShowSeatTest {

    @Test
    public void cannotReserveAlreadyReservedSeat() {
        var showSeat = new ShowSeat(null, 5);
        showSeat.doReserveForUser(new Buyer(1), LocalDateTime.now().plusMinutes(10));
        var e = assertThrows(ShowsException.class, () -> {
            showSeat.doReserveForUser(new Buyer(2), LocalDateTime.now().plusMinutes(10));
        });
        assertEquals(ShowSeat.SEAT_BUSY, e.getMessage());
    }

    @Test
    public void cannotConfirmNotReservedSeat() {
        var showSeat = new ShowSeat(null, 5);
        var aBuyer = new Buyer(1);
        showSeat.doReserveForUser(aBuyer, LocalDateTime.now().plusMinutes(10));
        showSeat.doConfirmForUser(aBuyer);
        var e = assertThrows(ShowsException.class, () -> {
            showSeat.doConfirmForUser(aBuyer);
        });
        assertEquals(ShowSeat.SEAT_NOT_RESERVED_OR_ALREADY_CONFIRMED, e.getMessage());
    }

    @Test
    public void cannotConfirmAlreadyConfirmedSeat() {
        var showSeat = new ShowSeat(null, 5);
        var e = assertThrows(ShowsException.class, () -> {
            showSeat.doConfirmForUser(new Buyer(2));
        });
        assertEquals(ShowSeat.SEAT_NOT_RESERVED_OR_ALREADY_CONFIRMED, e.getMessage());
    }
}
