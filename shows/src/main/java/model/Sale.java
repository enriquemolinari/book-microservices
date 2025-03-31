package model;

import api.ShowsException;
import api.Ticket;
import common.FormattedDateTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static model.Schema.DATABASE_SCHEMA_NAME;

@Entity
@Table(schema = DATABASE_SCHEMA_NAME)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
class Sale {
    public static final String SALE_CANNOT_BE_CREATED_WITHOUT_SEATS = "Sale cannot be created without seats";
    private String salesIdentifier;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private float total;
    private LocalDateTime salesDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private Buyer purchaser;
    private int pointsWon;

    @OneToMany
    @JoinColumn(name = "id_sale")
    private Set<ShowSeat> seatsSold;

    private Sale(String salesIdentifier, float totalAmount,
                 Buyer buyerThatPurchased,
                 Set<ShowSeat> seatsSold,
                 int pointsWon) {
        this.salesIdentifier = salesIdentifier;
        this.total = totalAmount;
        this.purchaser = buyerThatPurchased;
        this.seatsSold = seatsSold;
        this.salesDate = LocalDateTime.now();
        this.pointsWon = pointsWon;
        buyerThatPurchased.newPurchase(this);
    }

    public static Ticket registerNewSaleFor(String salesIdentifier, Buyer buyerThatPurchased,
                                            float totalAmount,
                                            int pointsWon,
                                            Set<ShowSeat> seatsSold) {
        checkSeatsNotEmpty(seatsSold);
        return new Sale(salesIdentifier, totalAmount, buyerThatPurchased, seatsSold,
                pointsWon).ticket();
    }

    private static void checkSeatsNotEmpty(Set<ShowSeat> seatsSold) {
        if (seatsSold.isEmpty()) {
            throw new ShowsException(SALE_CANNOT_BE_CREATED_WITHOUT_SEATS);
        }
    }

    private String formattedSalesDate() {
        return new FormattedDateTime(salesDate).toString();
    }

    List<Integer> confirmedSeatNumbers() {
        return this.seatsSold.stream().map(seat -> seat.seatNumber()).toList();
    }

    private Ticket ticket() {
        ShowSeat first = this.seatsSold.stream().findFirst().get();
        Long movieId = first.showMovieId();
        String startTime = first.showStartTime();
        return new Ticket(this.salesIdentifier, total,
                pointsWon,
                formattedSalesDate(),
                confirmedSeatNumbers(),
                movieId,
                startTime);
    }

    long points() {
        return this.pointsWon;
    }
}
