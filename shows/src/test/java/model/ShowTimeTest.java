package model;

import api.ShowsException;
import common.DateTimeProvider;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ShowTimeTest {

    private final ForTests tests = new ForTests();

//    @Test
//    public void showTimeStartTimeMustBeInTheFuture() {
//        Exception e = assertThrows(ShowsException.class, () -> {
//            new ShowTime(DateTimeProvider.create(),
//                    tests.createSmallFishMovie(),
//                    LocalDateTime.of(2023, 3, 10, 15, 0, 0, 0), 10f,
//                    new Theater("A Theater", Set.of(1)));
//        });
//
//        assertEquals(e.getMessage(), ShowTime.START_TIME_MUST_BE_IN_THE_FUTURE);
//    }

//    @Test
//    public void showTimeStartTimeMustBeAfterMovieReleaseDate() {
//        Exception e = assertThrows(ShowsException.class, () -> {
//            new ShowTime(DateTimeProvider.create(),
//                    tests.createSmallFishMovie(LocalDate.now().plusDays(20)),
//                    LocalDateTime.now().plusMinutes(10), 10f,
//                    new Theater("A Theater", Set.of(1)));
//        });
//
//        assertEquals(e.getMessage(),
//                ShowTime.SHOW_START_TIME_MUST_BE_AFTER_MOVIE_RELEASE_DATE);
//    }

    @Test
    public void showTimePriceMustNotBeFree() {
        Exception e = assertThrows(ShowsException.class, () -> {
            new ShowTime(DateTimeProvider.create(),
                    new Movie(1L),
                    LocalDateTime.now().plusDays(1), 0f, new Theater(
                    "A Theater", Set.of(1)));
        });

        assertEquals(ShowTime.PRICE_MUST_BE_POSITIVE, e.getMessage());
    }

    @Test
    public void createShowTime() {
        var aShow = tests.createShowForSmallFish();
        assertEquals(10f, aShow.price());
//        assertEquals("Small Fish", aShow.movieName());
        assertTrue(aShow.hasSeatNumbered(1));
        assertTrue(aShow.hasSeatNumbered(2));
        assertFalse(aShow.hasSeatNumbered(8));
    }

    @Test
    public void reservationHasExpired() {
        var aShow = tests.createShowForSmallFish();
        var carlos = createCarlosUser();
        var seatsToReserve = Set.of(1, 2);
        aShow.reserveSeatsFor(carlos, seatsToReserve, LocalDateTime.now().minusMinutes(6));
        assertFalse(aShow.areAllSeatsReservedBy(carlos, seatsToReserve));
    }

    @Test
    public void iCanReserveAnExpiredReservation() {
        var aShow = tests.createShowForSmallFish();
        var carlos = createCarlosUser();
        var jose = createJoseUser();
        var seatsToReserve = Set.of(1, 2);
        aShow.reserveSeatsFor(carlos, seatsToReserve, LocalDateTime.now().minusMinutes(6));
        aShow.reserveSeatsFor(jose, seatsToReserve, LocalDateTime.now().plusMinutes(6));
    }

    @Test
    public void reserveAnAvailableSeat() {
        var aShow = tests.createShowForSmallFish();
        var carlos = createCarlosUser();
        var seatsToReserve = Set.of(1, 2);
        aShow.reserveSeatsFor(carlos, seatsToReserve, LocalDateTime.now().plusMinutes(6));
        assertTrue(aShow.areAllSeatsReservedBy(carlos, seatsToReserve));
    }

    @Test
    public void reserveAlreadyReservedSeats() {
        var aShow = tests.createShowForSmallFish();
        var carlos = createCarlosUser();
        var jose = createJoseUser();
        var seatsToReserveByCarlos = Set.of(1, 2);
        aShow.reserveSeatsFor(carlos, seatsToReserveByCarlos, LocalDateTime.now().plusMinutes(6));
        var seatsToTryReserveByJose = Set.of(2, 3);
        Exception e = assertThrows(ShowsException.class, () -> {
            aShow.reserveSeatsFor(carlos, seatsToTryReserveByJose, LocalDateTime.now().plusMinutes(6));
        });
        assertEquals(ShowTime.SELECTED_SEATS_ARE_BUSY, e.getMessage());
        assertTrue(aShow.areAllSeatsReservedBy(carlos, seatsToReserveByCarlos));
        assertTrue(aShow.noneOfTheSeatsAreReservedBy(jose,
                seatsToTryReserveByJose));
    }

    @Test
    public void confirmReservedSeats() {
        var aShow = tests.createShowForSmallFish();
        var carlos = createCarlosUser();
        var seatsToReserveByCarlos = Set.of(1, 2);
        aShow.reserveSeatsFor(carlos, seatsToReserveByCarlos, LocalDateTime.now().plusMinutes(6));
        var seatsToConfirmByCarlos = Set.of(1, 2);
        aShow.confirmSeatsForUser(carlos, seatsToConfirmByCarlos);
        assertTrue(
                aShow.areAllSeatsConfirmedBy(carlos, seatsToConfirmByCarlos));
        assertEquals(seatsToConfirmByCarlos,
                Set.copyOf(aShow.confirmedSeatsFrom(carlos)));
        assertTrue(aShow.noneOfTheSeatsAreConfirmedBy(carlos,
                Set.of(3, 4, 5, 6)));
    }

    @Test
    public void notAllSeatsAreReserved() {
        var aShow = tests.createShowForSmallFish();
        var carlos = createCarlosUser();
        aShow.reserveSeatsFor(carlos, Set.of(1, 2), LocalDateTime.now().plusMinutes(6));
        assertFalse(aShow.areAllSeatsReservedBy(carlos, Set.of(1, 2, 5)));
    }

    @Test
    public void confirmNonReservedSeats() {
        var aShow = tests.createShowForSmallFish();
        var carlos = createCarlosUser();
        var seatsToReserveByCarlos = Set.of(1, 2, 4, 5);
        aShow.reserveSeatsFor(carlos, seatsToReserveByCarlos, LocalDateTime.now().plusMinutes(6));
        var seatsToConfirmByCarlos = Set.of(5, 6, 7);
        Exception e = assertThrows(ShowsException.class, () -> {
            aShow.confirmSeatsForUser(carlos, seatsToConfirmByCarlos);
        });
        assertEquals(ShowTime.RESERVATION_IS_REQUIRED_TO_CONFIRM,
                e.getMessage());
        assertTrue(aShow.areAllSeatsReservedBy(carlos, seatsToReserveByCarlos));
        assertTrue(aShow.noneOfTheSeatsAreConfirmedBy(carlos,
                seatsToConfirmByCarlos));
    }

    private Buyer createCarlosUser() {
        return new Buyer(1L);
    }

    private Buyer createJoseUser() {
        return new Buyer(2L);
    }
}
