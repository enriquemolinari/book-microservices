package model;

import api.*;
import common.DateTimeProvider;
import common.Tx;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

public class Shows implements ShowsSubSystem {
    static final int MINUTES_TO_KEEP_RESERVATION = 5;
    static final String MOVIE_ID_DOES_NOT_EXISTS = "Movie ID not found";
    static final String SHOW_TIME_ID_NOT_EXISTS = "Show ID not found";
    static final String BUYER_ID_NOT_EXISTS = "User not registered";
    static final String THEATER_ID_DOES_NOT_EXISTS = "Theater id not found";
    private final EntityManagerFactory emf;
    private final CreditCardPaymentProvider paymentGateway;
    private final DateTimeProvider dateTimeProvider;

    public Shows(EntityManagerFactory emf,
                 CreditCardPaymentProvider paymentGateway,
                 DateTimeProvider provider) {
        this.emf = emf;
        this.paymentGateway = paymentGateway;
        this.dateTimeProvider = provider;
    }

    public Shows(EntityManagerFactory emf,
                 CreditCardPaymentProvider paymentGateway) {
        this(emf, paymentGateway, DateTimeProvider.create());
    }

    @Override
    public List<MovieShows> showsUntil(LocalDateTime untilTo) {
        return new Tx(emf).inTx(em -> {
            return movieShowsUntil(untilTo, em);
        });
    }

    private List<MovieShows> movieShowsUntil(LocalDateTime untilTo, EntityManager em) {
        var query = em.createQuery(
                        "from Movie m "
                                + "join fetch m.showTimes s join fetch s.screenedIn "
                                + "where s.startTime >= ?1 and s.startTime <= ?2 "
                                + "order by m.name asc",
                        Movie.class).setParameter(1, LocalDateTime.now())
                .setParameter(2, untilTo);
        return query.getResultList().stream()
                .map(Movie::toMovieShow)
                .toList();
    }


    @Override
    public Long addNewTheater(String name, Set<Integer> seatsNumbers) {
        return new Tx(emf).inTx(em -> {
            var theater = new Theater(name, seatsNumbers);
            em.persist(theater);
            return theater.id();
        });
    }

    @Override
    public ShowInfo addNewShowFor(Long movieId, LocalDateTime startTime,
                                  float price, Long theaterId, int pointsToWin) {
        return new Tx(emf).inTx(em -> {
            var movie = movieBy(movieId, em);
            var theatre = theatreBy(theaterId, em);
            var showTime = new ShowTime(movie, startTime, price, theatre,
                    pointsToWin);
            em.persist(showTime);
            return showTime.toShowInfo();
        });
    }

    @Override
    public DetailedShowInfo reserve(Long buyerId, Long showTimeId,
                                    Set<Integer> selectedSeats) {
        return new Tx(emf).inTx(em -> {
            ShowTime showTime = showTimeBy(showTimeId, em);
            var user = buyerBy(buyerId, em);
            showTime.reserveSeatsFor(user, selectedSeats,
                    this.dateTimeProvider.now().plusMinutes(MINUTES_TO_KEEP_RESERVATION));
            return showTime.toDetailedInfo();
        });
    }

    @Override
    public Ticket pay(Long userId, Long showTimeId, Set<Integer> selectedSeats,
                      String creditCardNumber, YearMonth expirationDate,
                      String secturityCode) {
        return new Tx(emf).inTx(em -> {
            ShowTime showTime = showTimeBy(showTimeId, em);
            var user = buyerBy(userId, em);
            var ticket = new Cashier(this.paymentGateway).paySeatsFor(selectedSeats,
                    showTime,
                    user,
                    CreditCard.of(creditCardNumber, expirationDate, secturityCode));
            //TODO: publish event in Rabbit
//            this.publisher.notify(em, new TicketsSoldEvent(userId,
//                    ticket.getPointsWon(),
//                    ticket.total(),
//                    ticket.getPayedSeats(),
//                    ticket.getMovieName(),
//                    ticket.getShowStartTime()));
            return ticket;
        });
    }

    Long addNewMovie(Long id, String name, int duration, LocalDate releaseDate, Set<String> genres) {
        return new Tx(emf).inTx(em -> {
            em.persist(new Movie(id, name, duration, releaseDate, genres));
            return id;
        });
    }

    Long addNewBuyer(Long id) {
        return new Tx(emf).inTx(em -> {
            em.persist(new Buyer(id));
            return id;
        });
    }

    private Theater theatreBy(Long theatreId, EntityManager em) {
        return findByIdOrThrows(Theater.class, theatreId, THEATER_ID_DOES_NOT_EXISTS, em);
    }

    private Movie movieBy(Long movieId, EntityManager em) {
        return findByIdOrThrows(Movie.class, movieId, MOVIE_ID_DOES_NOT_EXISTS, em);
    }

    private Buyer buyerBy(Long buyerId, EntityManager em) {
        return findByIdOrThrows(Buyer.class, buyerId, BUYER_ID_NOT_EXISTS, em);
    }

    private ShowTime showTimeBy(Long id, EntityManager em) {
        return findByIdOrThrows(ShowTime.class, id, SHOW_TIME_ID_NOT_EXISTS, em);
    }

    <T> T findByIdOrThrows(Class<T> entity, Long id, String msg, EntityManager em) {
        var e = em.find(entity, id);
        if (e == null) {
            throw new ShowsException(msg);
        }
        return e;
    }

    @Override
    public DetailedShowInfo show(Long id) {
        return new Tx(emf).inTx(em -> {
            var show = showTimeBy(id, em);
            return show.toDetailedInfo();
        });
    }
}