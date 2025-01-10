package model;

import api.MovieShows;
import api.Seat;
import api.ShowsException;
import api.ShowsSubSystem;
import common.DateTimeProvider;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Set;

import static model.MoviesHttpSyncCallProviderTest.VALID_MOVIES_URL;
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
        var movieId = tests.createAMovie(shows, 1L);
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
        var movieId = tests.createAMovie(shows, 1L);
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
    public void showsPlayingAt() {
        try (ClientAndServer mockServer = tests.setUpMockServerExpectation(jsonTwoMovies())) {
            var shows = createShowsSubSystemMockedMoviesProvider(DateTimeProvider.create());
            tests.createAMovie(shows, 1L);
            tests.createAMovie(shows, 2L);
            long theaterId = createATheater(shows);
            shows.addNewShowFor(1L,
                    LocalDateTime.now().plusDays(2),
                    15f, theaterId, 20);
            shows.addNewShowFor(1L,
                    LocalDateTime.now().plusDays(1), 16f, theaterId, 20);
            shows.addNewShowFor(2L,
                    LocalDateTime.now().plusDays(4), 30f, theaterId, 30);
            var movieShows = shows
                    .showsUntil(
                            LocalDateTime.of(LocalDate.now().plusDays(5).getYear(),
                                    10, 10, 13, 31));
            assertEquals(2, movieShows.size());
            MovieShows movieOne = movieShows.stream().filter(m -> m.movieId().equals(1L)).toList().getFirst();
            MovieShows movieTwo = movieShows.stream().filter(m -> m.movieId().equals(2L)).toList().getFirst();
            assertEquals(2, movieOne.shows().size());
            assertEquals(1, movieTwo.shows().size());
            assertEquals("super movie name", movieOne.movieName());
            assertEquals("other movie name", movieTwo.movieName());
            assertEquals("2hrs 05mins", movieOne.duration());
            assertEquals("3hrs 15mins", movieTwo.duration());
            assertEquals(2, movieOne.genres().size());
            assertEquals(1, movieTwo.genres().size());
            assertEquals(15f, movieOne.shows().stream().filter(s -> s.showId().equals(1L)).toList().getFirst().price());
            assertEquals(16f, movieOne.shows().stream().filter(s -> s.showId().equals(2L)).toList().getFirst().price());
            assertEquals(30f, movieTwo.shows().getFirst().price());
        }
    }

    @Test
    public void aShowIsPlayingAt() {
        try (ClientAndServer mockServer = tests.setUpMockServerExpectation(jsonOneMovie())) {
            var shows = createShowsSubSystemMockedMoviesProvider(DateTimeProvider.create());
            var movieId = tests.createAMovie(shows, 1L);
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
            assertEquals("2hrs 05mins", movieShows.getFirst().duration());
            assertEquals(1, movieShows.getFirst().shows().size());
            assertEquals(10f, movieShows.getFirst().shows().getFirst().price());
            assertEquals("A movie name", movieShows.getFirst().movieName());
            assertEquals(Set.of("genre1", "genre2"), movieShows.getFirst().genres());
        }
    }

    @Test
    public void reserveSeats() {
        var shows = createShowsSubSystem(DateTimeProvider.create());
        var movieId = tests.createAMovie(shows, 1L);
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
        var movieId = tests.createAMovie(shows, 1L);
        long theaterId = createATheater(shows);
        var showInfo = shows.addNewShowFor(movieId,
                LocalDateTime.of(LocalDate.now().plusYears(1).getYear(), 10, 10,
                        13, 30),
                10f, theaterId, 20);
        var userId = registerAUser(shows);
        shows.reserve(userId, showInfo.showId(), Set.of(1, 5));
        var info = shows.show(showInfo.showId());
        assertTrue(info.currentSeats().contains(new Seat(1, false)));
        assertTrue(info.currentSeats().contains(new Seat(2, true)));
        assertTrue(info.currentSeats().contains(new Seat(3, true)));
        assertTrue(info.currentSeats().contains(new Seat(4, true)));
        assertTrue(info.currentSeats().contains(new Seat(5, false)));
    }

    @Test
    public void reserveAlreadReservedSeats() {
        var shows = createShowsSubSystem(DateTimeProvider.create());
        var movieId = tests.createAMovie(shows, 1L);
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
        var shows = new Shows(emf, fakePaymenentProvider, tests.doNothingMoviesProvider());
        var movieId = tests.createAMovie(shows, 1L);
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
        var movieId = tests.createAMovie(shows, 1L);
        var e = assertThrows(ShowsException.class, () -> {
            shows.addNewShowFor(movieId, LocalDateTime.now().plusDays(1), 10f, NON_EXISTENT_ID, 10);
            fail("ShowId should not exists in the database");
        });
        assertEquals(Shows.THEATER_ID_DOES_NOT_EXISTS, e.getMessage());
    }

    private Shows createShowsSubSystemMockedMoviesProvider(DateTimeProvider dateTimeProvider) {
        return new Shows(emf,
                tests.doNothingPaymentProvider(),
                new MoviesHttpSyncCallProvider(VALID_MOVIES_URL),
                dateTimeProvider);
    }

    private Shows createShowsSubSystem(DateTimeProvider dateTimeProvider) {
        return new Shows(emf,
                tests.doNothingPaymentProvider(),
                tests.doNothingMoviesProvider(),
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

    private String jsonOneMovie() {
        return """
                [
                    {
                        "id": 1,
                        "name": "A movie name",
                        "duration": "2hrs 05mins",
                        "genres": [
                            "genre1",
                            "genre2"
                        ]
                    }
                ]
                """;
    }

    private String jsonTwoMovies() {
        return """
                [
                    {
                        "id": 1,
                        "name": "super movie name",
                        "duration": "2hrs 05mins",
                        "genres": [
                            "genre1",
                            "genre2"
                        ]
                    },
                    {
                        "id": 2,
                        "name": "other movie name",
                        "duration": "3hrs 15mins",
                        "genres": [
                            "genre3"
                        ]
                    }
                ]
                """;
    }

}
