package model;

import api.DetailedShowInfo;
import api.ShowInfo;
import api.ShowsException;
import common.DateTimeProvider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static model.Schema.DATABASE_SCHEMA_NAME;

@Entity
@Table(schema = DATABASE_SCHEMA_NAME)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
public class ShowTime {

    static final String START_TIME_MUST_BE_IN_THE_FUTURE = "The show start time must be in the future";
    static final String PRICE_MUST_BE_POSITIVE = "The price must be greater than zero";
    static final String SELECTED_SEATS_ARE_BUSY = "All or some of the seats chosen are busy";
    static final String RESERVATION_IS_REQUIRED_TO_CONFIRM = "Reservation is required before confirm";
    private static final int DEFAULT_TOTAL_POINTS_FOR_A_PURCHASE = 10;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private LocalDateTime startTime;

    //@Transient
    // When hibernate creates an instance of this class, this will be
    // null if I don't initialize it here.
    private transient DateTimeProvider timeProvider = DateTimeProvider.create();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_movie")
    private Movie movieToBeScreened;
    private float price;
    @ManyToOne(fetch = FetchType.LAZY)
    private Theater screenedIn;
    @OneToMany(mappedBy = "show", cascade = CascadeType.PERSIST)
    private Set<ShowSeat> seatsForThisShow;
    @Column(name = "pointsToWin")
    private int pointsThatAUserWin;

    public ShowTime(DateTimeProvider provider, Movie movie,
                    LocalDateTime startTime, float price, Theater screenedIn) {
        this(provider, movie, startTime, price, screenedIn,
                DEFAULT_TOTAL_POINTS_FOR_A_PURCHASE);
    }

    public ShowTime(Movie movie, LocalDateTime startTime, float price,
                    Theater screenedIn, int totalPointsToWin) {
        this(DateTimeProvider.create(), movie, startTime, price, screenedIn,
                totalPointsToWin);
    }

    public ShowTime(DateTimeProvider provider, Movie movie,
                    LocalDateTime startTime, float price, Theater screenedIn,
                    int totalPointsToWin) {
        this.timeProvider = provider;
        this.movieToBeScreened = movie;
        checkStartTimeIsInTheFuture(startTime);
        checkPriceIsPositiveAndNotFree(price);
        this.price = price;
        this.startTime = startTime;
        this.screenedIn = screenedIn;
        this.seatsForThisShow = screenedIn.seatsForShow(this);
        this.pointsThatAUserWin = totalPointsToWin;
    }

    private Set<ShowSeat> filterSelectedSeats(Set<Integer> selectedSeats) {
        return this.seatsForThisShow.stream()
                .filter(seat -> seat.isIncludedIn(selectedSeats))
                .collect(Collectors.toUnmodifiableSet());
    }

    void reserveSeatsFor(Buyer buyer, Set<Integer> selectedSeats, LocalDateTime reservedUntil) {
        var selection = filterSelectedSeats(selectedSeats);
        checkAllSelectedSeatsAreAvailable(selection);
        reserveAllSeatsFor(buyer, selection, reservedUntil);
    }

    int pointsToEarn() {
        return this.pointsThatAUserWin;
    }

    float totalAmountForTheseSeats(Set<Integer> selectedSeats) {
        return Math.round(selectedSeats.size() * this.price * 100.0f) / 100.0f;
    }

    Set<ShowSeat> confirmSeatsForUser(Buyer buyer, Set<Integer> selectedSeats) {
        var selection = filterSelectedSeats(selectedSeats);
        checkAllSelectedSeatsAreReservedBy(buyer, selection);
        confirmAllSeatsFor(buyer, selection);
        return selection;
    }

    private void checkPriceIsPositiveAndNotFree(float price) {
        if (price <= 0) {
            throw new ShowsException(PRICE_MUST_BE_POSITIVE);
        }
    }

    private void checkStartTimeIsInTheFuture(LocalDateTime startTime) {
        if (startTime.isBefore(this.timeProvider.now())) {
            throw new ShowsException(START_TIME_MUST_BE_IN_THE_FUTURE);
        }
    }

    public boolean hasSeatNumbered(int aSeatNumber) {
        return this.seatsForThisShow.stream()
                .anyMatch(seat -> seat.isSeatNumbered(aSeatNumber));
    }

    boolean noneOfTheSeatsAreReservedBy(Buyer aBuyer,
                                        Set<Integer> seatsToReserve) {
        return !areAllSeatsReservedBy(aBuyer, seatsToReserve);
    }

    public boolean noneOfTheSeatsAreConfirmedBy(Buyer carlos,
                                                Set<Integer> seatsToConfirmByCarlos) {
        return !areAllSeatsConfirmedBy(carlos, seatsToConfirmByCarlos);
    }

    boolean areAllSeatsConfirmedBy(Buyer aBuyer, Set<Integer> seatsToReserve) {
        var selectedSeats = filterSelectedSeats(seatsToReserve);
        return allMatchConditionFor(selectedSeats,
                seat -> seat.isConfirmedBy(aBuyer));
    }

    boolean areAllSeatsReservedBy(Buyer aBuyer, Set<Integer> seatsToReserve) {
        var selectedSeats = filterSelectedSeats(seatsToReserve);
        return allMatchConditionFor(selectedSeats,
                seat -> seat.isReservedBy(aBuyer));
    }

    private void checkAtLeastOneMatchConditionFor(Set<ShowSeat> seatsToReserve,
                                                  Predicate<ShowSeat> condition, String errorMsg) {
        if (seatsToReserve.stream().anyMatch(condition)) {
            throw new ShowsException(errorMsg);
        }
    }

    private boolean allMatchConditionFor(Set<ShowSeat> seatsToReserve,
                                         Predicate<ShowSeat> condition) {
        return seatsToReserve.stream().allMatch(condition);
    }

    private void reserveAllSeatsFor(Buyer buyer, Set<ShowSeat> selection, LocalDateTime reservedUntil) {
        selection.forEach(seat -> seat.doReserveForUser(buyer, reservedUntil));
    }

    private void confirmAllSeatsFor(Buyer buyer, Set<ShowSeat> selection) {
        selection.forEach(seat -> seat.doConfirmForUser(buyer));
    }

    private void checkAllSelectedSeatsAreAvailable(Set<ShowSeat> selection) {
        checkAtLeastOneMatchConditionFor(selection, ShowSeat::isBusy,
                SELECTED_SEATS_ARE_BUSY);
    }

    private void checkAllSelectedSeatsAreReservedBy(Buyer buyer,
                                                    Set<ShowSeat> selection) {
        checkAtLeastOneMatchConditionFor(selection,
                seat -> !seat.isReservedBy(buyer),
                RESERVATION_IS_REQUIRED_TO_CONFIRM);
    }

    float price() {
        return this.price;
    }

    String startDateTime() {
        return new FormattedDayTime(this.startTime).toString();
    }

    public ShowInfo toShowInfo() {
        return new ShowInfo(this.id,
                startDateTime(),
                this.price,
                this.movieToBeScreened.id());
    }

    public DetailedShowInfo toDetailedInfo() {
        return new DetailedShowInfo(this.toShowInfo(),
                this.screenedIn.name(),
                this.seatsForThisShow.stream().map(ShowSeat::toSeat).toList());
    }

    List<Integer> confirmedSeatsFrom(Buyer purchaser) {
        return this.seatsForThisShow.stream()
                .filter(seat -> seat.isConfirmedBy(purchaser))
                .map(ShowSeat::seatNumber).toList();
    }

    Long movieId() {
        return this.movieToBeScreened.id();
    }
}
