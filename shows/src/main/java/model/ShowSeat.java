package model;

import api.Seat;
import api.ShowsException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

import static model.Schema.DATABASE_SCHEMA_NAME;

@Entity
@Table(schema = DATABASE_SCHEMA_NAME)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
public class ShowSeat {

    static final String SEAT_BUSY = "Seat is currently busy";
    static final String SEAT_NOT_RESERVED_OR_ALREADY_CONFIRMED = "The seat cannot be confirmed";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Buyer buyer;
    private boolean reserved;
    private boolean confirmed;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_show")
    private ShowTime show;
    private LocalDateTime reservedUntil;
    private Integer seatNumber;
    @Version
    private int version;

    public ShowSeat(ShowTime s, Integer seatNumber) {
        this.show = s;
        this.seatNumber = seatNumber;
        this.reserved = false;
        this.confirmed = false;
    }

    public void doReserveForUser(Buyer buyer, LocalDateTime until) {
        if (!isAvailable()) {
            throw new ShowsException(SEAT_BUSY);
        }
        this.reserved = true;
        this.buyer = buyer;
        this.reservedUntil = until;
    }

    public boolean isBusy() {
        return !isAvailable();
    }

    public boolean isAvailable() {
        return (!reserved || LocalDateTime.now().isAfter(this.reservedUntil)) && !confirmed;
    }

    public void doConfirmForUser(Buyer buyer) {
        if (!isReservedBy(buyer) || confirmed) {
            throw new ShowsException(SEAT_NOT_RESERVED_OR_ALREADY_CONFIRMED);
        }
        this.confirmed = true;
        this.buyer = buyer;
    }

    boolean isConfirmedBy(Buyer buyer) {
        if (this.buyer == null) {
            return false;
        }
        return confirmed && this.buyer.equals(buyer);
    }

    boolean isReservedBy(Buyer buyer) {
        if (this.buyer == null) {
            return false;
        }
        return reserved && this.buyer.equals(buyer)
                && LocalDateTime.now().isBefore(this.reservedUntil);
    }

    public boolean isSeatNumbered(int aSeatNumber) {
        return this.seatNumber.equals(aSeatNumber);
    }

    public boolean isIncludedIn(Set<Integer> selectedSeats) {
        return selectedSeats.stream()
                .anyMatch(ss -> ss.equals(this.seatNumber));
    }

    int seatNumber() {
        return seatNumber;
    }

    public Seat toSeat() {
        return new Seat(seatNumber, isAvailable());
    }

    public Long showMovieId() {
        return this.show.movieId();
    }

    public String showStartTime() {
        return this.show.startDateTime();
    }
}
