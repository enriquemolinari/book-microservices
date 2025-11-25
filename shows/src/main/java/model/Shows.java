package model;

import api.*;
import common.DateTimeProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import model.events.publish.NewTicketsSoldEvent;
import model.queue.JQueueInTxtQueue;
import model.queue.JQueueTable;

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
    private final SalesIdentifierGenerator identifierGenerator;

    public Shows(EntityManagerFactory emf,
                 CreditCardPaymentProvider paymentGateway,
                 DateTimeProvider provider, SalesIdentifierGenerator identifierGenerator) {
        this.emf = emf;
        this.paymentGateway = paymentGateway;
        this.dateTimeProvider = provider;
        this.identifierGenerator = identifierGenerator;
    }

    public Shows(EntityManagerFactory emf,
                 CreditCardPaymentProvider paymentGateway) {
        this(emf, paymentGateway, DateTimeProvider.create(), new UUIDSalesIdentifierGenerator());
    }

    @Override
    public List<MovieShows> showsUntil(LocalDateTime untilTo) {
        return emf.callInTransaction(em -> {
            return movieShowsUntil(untilTo, em);
        });
    }

    private List<MovieShows> movieShowsUntil(LocalDateTime untilTo, EntityManager em) {
        List<Movie> movies = em.createQuery(
                        "from Movie m "
                                + "join fetch m.showTimes s join fetch s.screenedIn "
                                + "where s.startTime >= ?1 and s.startTime <= ?2 ",
                        Movie.class).setParameter(1, LocalDateTime.now())
                .setParameter(2, untilTo).getResultList();
        return movies.stream()
                .map(Movie::toMovieShow)
                .toList();
    }

    @Override
    public Long addNewTheater(String name, Set<Integer> seatsNumbers) {
        return emf.callInTransaction(em -> {
            var theater = new Theater(name, seatsNumbers);
            em.persist(theater);
            return theater.id();
        });
    }

    @Override
    public ShowInfo addNewShowFor(Long movieId, LocalDateTime startTime,
                                  float price, Long theaterId, int pointsToWin) {
        return emf.callInTransaction(em -> {
            //this method is not validating that shows are not overlapping
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
        return emf.callInTransaction(em -> {
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
        return emf.callInTransaction(em -> {
            ShowTime showTime = showTimeBy(showTimeId, em);
            var user = buyerBy(userId, em);
            var ticket = new Cashier(this.identifierGenerator, this.paymentGateway)
                    .paySeatsFor(selectedSeats,
                            showTime,
                            user,
                            CreditCard.of(creditCardNumber, expirationDate, secturityCode));
            new JQueueInTxtQueue(em).push(new NewTicketsSoldEvent(ticket.getSalesId()).toJson());
            return ticket;
        });
    }

    @Override
    public SaleInfo sale(String salesIdentifier) {
        return emf.callInTransaction(em -> {
            var list = em.createQuery("from Sale where salesIdentifier = :saleid", Sale.class)
                    .setParameter("saleid", salesIdentifier)
                    .getResultList();
            if (list.isEmpty()) {
                throw new ShowsException("Sale not found");
            }
            Sale first = list.getFirst();
            return first.saleInfo();
        });
    }

    @Override
    public MovieShows movieShowsBy(Long movieId) {
        return emf.callInTransaction(em -> {
            return this.movieBy(movieId, em).toMovieShow();
        });
    }

    public BuyerInfo buyer(Long userId) {
        return emf.callInTransaction(em -> {
            return buyerInfoBy(userId).info();
        });
    }

    // used for testing only
    Long addNewMovie(Long id) {
        return emf.callInTransaction(em -> {
            em.persist(new Movie(id));
            return id;
        });
    }

    Long addNewBuyer(Long id) {
        return emf.callInTransaction(em -> {
            em.persist(new Buyer(id));
            return id;
        });
    }

    Buyer buyerInfoBy(Long buyerId) {
        return emf.callInTransaction(em -> {
            return findByIdOrThrows(Buyer.class, buyerId, BUYER_ID_NOT_EXISTS, em);
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
        return emf.callInTransaction(em -> {
            var show = showTimeBy(id, em);
            return show.toDetailedInfo();
        });
    }

    List<JQueueTable> allQueued() {
        return emf.callInTransaction(em -> {
            return em.createQuery("from JQueueTable", JQueueTable.class).getResultList();
        });
    }
}