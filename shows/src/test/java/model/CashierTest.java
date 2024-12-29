package model;

import api.ShowsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CashierTest {
    private final ForTests tests = new ForTests();
    private ShowTime aShow;
    private Buyer carlos;
    private Set<Integer> seatsForCarlos;

    private static YearMonth getExpirationDate() {
        return YearMonth.of(LocalDateTime.now().plusMonths(1).getYear(),
                LocalDateTime.now().plusMonths(1).getMonth().getValue());
    }

    @BeforeEach
    public void before() {
        aShow = tests.createShowForSmallFish();
        carlos = tests.createUserNicolas();
        seatsForCarlos = Set.of(1, 2);
    }

    @Test
    public void payOk() {
        reserveSeatsForCarlos(seatsForCarlos);
        var paymentProvider = tests.fakePaymenentProvider();
        var cashier = new Cashier(paymentProvider);
        YearMonth expirationDate = getExpirationDate();
        var ticket = cashier.paySeatsFor(seatsForCarlos, aShow, carlos,
                CreditCard.of("789456",
                        expirationDate,
                        "123456"));
        assertEquals(20f, ticket.total());
        assertEquals("Small Fish", ticket.getMovieName());
        assertEquals(10, ticket.getPointsWon());
        assertTrue(ticket.hasSeats(seatsForCarlos));
        assertTrue(paymentProvider.hasBeanCalledWith("789456",
                expirationDate,
                "123456",
                20f));
    }

    @Test
    public void payWithoutSeatsCannotBeCompleted() {
        Exception e = assertThrows(ShowsException.class, () -> {
            var ticket = new Cashier(tests.fakePaymenentProvider())
                    .paySeatsFor(Set.of(), aShow, carlos,
                            CreditCard.of("789456",
                                    getExpirationDate(),
                                    "123456"));
        });
        assertEquals(e.getMessage(), Cashier.SELECTED_SEATS_SHOULD_NOT_BE_EMPTY);
    }

    private void reserveSeatsForCarlos(Set<Integer> seatsForCarlos) {
        aShow.reserveSeatsFor(carlos, seatsForCarlos, LocalDateTime.now().plusMinutes(15));
    }

    @Test
    public void paymentProviderRejectingCreditCard() {
        reserveSeatsForCarlos(seatsForCarlos);
        var paymentProvider = tests.fakePaymenentProviderThrowE();
        var cashier = new Cashier(paymentProvider);
        YearMonth expirationDate = getExpirationDate();
        Exception e = assertThrows(ShowsException.class, () -> {
            cashier.paySeatsFor(seatsForCarlos, aShow, carlos,
                    CreditCard.of("789456",
                            expirationDate,
                            "123456"));
        });
        assertEquals(e.getMessage(), Cashier.CREDIT_CARD_DEBIT_HAS_FAILED);
        assertTrue(aShow.noneOfTheSeatsAreConfirmedBy(carlos, seatsForCarlos));
    }
}