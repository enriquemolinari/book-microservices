package model;

import api.Seat;
import api.ShowsException;
import api.ShowsSubSystem;
import common.DateTimeProvider;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Set;

import static model.ForTests.SUPER_MOVIE_NAME;
import static model.PersistenceUnit.DERBY_EMBEDDED_SHOWS_MS;
import static org.junit.jupiter.api.Assertions.*;

public class ShowsTest {
    private static final YearMonth JOSEUSER_CREDIT_CARD_EXPIRITY = YearMonth.of(
            LocalDateTime.now().getYear(),
            LocalDateTime.now().plusMonths(2).getMonth());
    private static final String JOSEUSER_CREDIT_CARD_SEC_CODE = "145";
    private static final String JOSEUSER_CREDIT_CARD_NUMBER = "123-456-789";
    private static final Long NON_EXISTENT_ID = -2L;
    private final ForTests tests = new ForTests();
    private EntityManagerFactory emf;

    @BeforeEach
    public void setUp() {
        emf = Persistence.createEntityManagerFactory(DERBY_EMBEDDED_SHOWS_MS);
    }

    @Test
    public void reservationHasExpired() {
        var shows = createShowsSubSystem(() -> LocalDateTime.now().minusMinutes(50));
        var movieId = tests.createSuperMovie(shows);
        var theaterId = createATheater(shows);
        var showInfo = shows.addNewShowFor(movieId,
                LocalDateTime.of(LocalDate.now().plusYears(1).getYear(), 10, 10,
                        13, 30),
                10f, theaterId, 20);
        var userId = registerUserJose(shows);
        shows.reserve(userId, showInfo.showId(), Set.of(1, 5));
        var e = assertThrows(ShowsException.class, () -> {
            shows.pay(userId, showInfo.showId(), Set.of(1, 5),
                    JOSEUSER_CREDIT_CARD_NUMBER,
                    JOSEUSER_CREDIT_CARD_EXPIRITY,
                    JOSEUSER_CREDIT_CARD_SEC_CODE);
        });
        assertEquals("Reservation is required before confirm", e.getMessage());
    }

    @Test
    public void iCanReserveAnExpiredReservation() {
        var shows = createShowsSubSystem(() -> LocalDateTime.now().minusMinutes(50));
        var movieId = tests.createSuperMovie(shows);
        var theaterId = createATheater(shows);
        var showInfo = shows.addNewShowFor(movieId,
                LocalDateTime.of(LocalDate.now().plusYears(1).getYear(), 10, 10,
                        13, 30),
                10f, theaterId, 20);
        var joseUserId = registerUserJose(shows);
        var userId = registerAUser(shows);
        shows.reserve(joseUserId, showInfo.showId(), Set.of(1, 5));
        // if exception is not thrown it means I was able to make the reservation
        var info = shows.reserve(userId, showInfo.showId(), Set.of(1, 5));
        // in any case all is available because I have reserved with a date provider in the past
        assertTrue(info.currentSeats().contains(new Seat(1, true)));
        assertTrue(info.currentSeats().contains(new Seat(2, true)));
        assertTrue(info.currentSeats().contains(new Seat(3, true)));
        assertTrue(info.currentSeats().contains(new Seat(4, true)));
        assertTrue(info.currentSeats().contains(new Seat(5, true)));
    }

    @Test
    public void aShowIsPlayingAt() {
        var shows = createShowsSubSystem(DateTimeProvider.create());
        var movieId = tests.createSuperMovie(shows);
        long theaterId = createATheater(shows);
        shows.addNewShowFor(movieId,
                LocalDateTime.of(LocalDate.now().plusYears(1).getYear(), 10, 10,
                        13, 30),
                10f, theaterId, 20);
        shows.addNewShowFor(movieId,
                LocalDateTime.of(LocalDate.now().plusYears(2).getYear(),
                        5, 10,
                        13, 30),
                10f, theaterId, 20);
        var movieShows = shows
                .showsUntil(
                        LocalDateTime.of(LocalDate.now().plusYears(1).getYear(),
                                10, 10, 13, 31));
        assertEquals(1, movieShows.size());
        assertEquals("1hr 49mins", movieShows.get(0).duration());
        assertEquals(1, movieShows.get(0).shows().size());
        assertEquals(10f, movieShows.get(0).shows().get(0).price());
        assertEquals(SUPER_MOVIE_NAME, movieShows.get(0).movieName());
    }

    @Test
    public void reserveSeats() {
        var shows = createShowsSubSystem(DateTimeProvider.create());
        var movieId = tests.createSuperMovie(shows);
        long theaterId = createATheater(shows);
        var showInfo = shows.addNewShowFor(movieId,
                LocalDateTime.of(LocalDate.now().plusYears(1).getYear(), 10, 10,
                        13, 30),
                10f, theaterId, 20);
        var userId = registerAUser(shows);
        var info = shows.reserve(userId, showInfo.showId(), Set.of(1, 5));
        assertTrue(info.currentSeats().contains(new Seat(1, false)));
        assertTrue(info.currentSeats().contains(new Seat(2, true)));
        assertTrue(info.currentSeats().contains(new Seat(3, true)));
        assertTrue(info.currentSeats().contains(new Seat(4, true)));
        assertTrue(info.currentSeats().contains(new Seat(5, false)));
    }

    @Test
    public void retrieveShow() {
        var shows = createShowsSubSystem(DateTimeProvider.create());
        var movieId = tests.createSuperMovie(shows);
        long theaterId = createATheater(shows);
        var showInfo = shows.addNewShowFor(movieId,
                LocalDateTime.of(LocalDate.now().plusYears(1).getYear(), 10, 10,
                        13, 30),
                10f, theaterId, 20);
        var userId = registerAUser(shows);
        shows.reserve(userId, showInfo.showId(), Set.of(1, 5));
        var info = shows.show(showInfo.showId());
        assertEquals(SUPER_MOVIE_NAME, info.info().movieName());
        assertEquals("1hr 49mins", info.info().movieDuration());
        assertTrue(info.currentSeats().contains(new Seat(1, false)));
        assertTrue(info.currentSeats().contains(new Seat(2, true)));
        assertTrue(info.currentSeats().contains(new Seat(3, true)));
        assertTrue(info.currentSeats().contains(new Seat(4, true)));
        assertTrue(info.currentSeats().contains(new Seat(5, false)));
    }

    @Test
    public void reserveAlreadReservedSeats() {
        var shows = createShowsSubSystem(DateTimeProvider.create());
        var movieId = tests.createSuperMovie(shows);
        long theaterId = createATheater(shows);
        var showInfo = shows.addNewShowFor(movieId,
                LocalDateTime.of(LocalDate.now().plusYears(1).getYear(), 10, 10,
                        13, 30),
                10f, theaterId, 20);
        var userId = registerAUser(shows);
        var joseId = registerUserJose(shows);
        shows.reserve(userId, showInfo.showId(), Set.of(1, 5));
        var e = assertThrows(ShowsException.class, () -> {
            shows.reserve(joseId, showInfo.showId(), Set.of(1, 4, 3));
            fail("I have reserved an already reserved seat");
        });
        assertEquals(ShowTime.SELECTED_SEATS_ARE_BUSY, e.getMessage());
    }

    @Test
    public void confirmAndPaySeats() {
        var fakePaymenentProvider = tests.fakePaymenentProvider();
        var shows = new Shows(emf, fakePaymenentProvider);
        var movieId = tests.createSuperMovie(shows);
        long theaterId = createATheater(shows);
        var showInfo = shows.addNewShowFor(movieId,
                LocalDateTime.of(LocalDate.now().plusYears(1).getYear(), 10, 10,
                        13, 30),
                10f, theaterId, 20);
        var joseId = registerUserJose(shows);
        shows.reserve(joseId, showInfo.showId(), Set.of(1, 5));
        var ticket = shows.pay(joseId, showInfo.showId(), Set.of(1, 5),
                JOSEUSER_CREDIT_CARD_NUMBER,
                JOSEUSER_CREDIT_CARD_EXPIRITY,
                JOSEUSER_CREDIT_CARD_SEC_CODE);
//TODO: add once decided queue mechanisms
        //        assertTrue(fakePublisher.invokedWithEvent(new TicketsSoldEvent(joseId,
//                ticket.getPointsWon(),
//                ticket.total(),
//                ticket.getPayedSeats(),
//                ticket.getMovieName(),
//                ticket.getShowStartTime())));
        assertTrue(ticket.hasSeats(Set.of(1, 5)));
        assertTrue(fakePaymenentProvider.hasBeanCalledWith(
                JOSEUSER_CREDIT_CARD_NUMBER,
                JOSEUSER_CREDIT_CARD_EXPIRITY, JOSEUSER_CREDIT_CARD_SEC_CODE,
                ticket.total()));
        var detailedShow = shows.show(showInfo.showId());
        assertTrue(detailedShow.currentSeats().contains(new Seat(1, false)));
        assertTrue(detailedShow.currentSeats().contains(new Seat(2, true)));
        assertTrue(detailedShow.currentSeats().contains(new Seat(3, true)));
        assertTrue(detailedShow.currentSeats().contains(new Seat(4, true)));
        assertTrue(detailedShow.currentSeats().contains(new Seat(5, false)));
    }

    @Test
    public void showTimeIdNotExists() {
        var shows = createShowsSubSystem(DateTimeProvider.create());
        var e = assertThrows(ShowsException.class, () -> {
            shows.show(NON_EXISTENT_ID);
            fail("ShowId should not exists in the database");
        });
        assertEquals(Shows.SHOW_TIME_ID_NOT_EXISTS, e.getMessage());
    }

    @Test
    public void theaterIdNotExists() {
        var shows = createShowsSubSystem(DateTimeProvider.create());
        var movieId = tests.createSuperMovie(shows);
        var e = assertThrows(ShowsException.class, () -> {
            shows.addNewShowFor(movieId, LocalDateTime.now().plusDays(1), 10f, NON_EXISTENT_ID, 10);
            fail("ShowId should not exists in the database");
        });
        assertEquals(Shows.THEATER_ID_DOES_NOT_EXISTS, e.getMessage());
    }

    private Shows createShowsSubSystem(DateTimeProvider dateTimeProvider) {
        return new Shows(emf,
                tests.doNothingPaymentProvider(),
                dateTimeProvider);
    }

    private Long registerUserJose(Shows shows) {
        return shows.addNewBuyer(1L);
    }

    private Long registerAUser(Shows shows) {
        return shows.addNewBuyer(3L);
    }

    private Long createATheater(ShowsSubSystem shows) {
        return shows.addNewTheater("a Theater",
                Set.of(1, 2, 3, 4, 5, 6));
    }

    @AfterEach
    public void tearDown() {
        emf.close();
    }

}
