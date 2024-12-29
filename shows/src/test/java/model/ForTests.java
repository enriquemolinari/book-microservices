package model;

import common.DateTimeProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Set;


public class ForTests {

    static final String SUPER_MOVIE_PLOT = "a super movie that shows the life of ...";
    static final String SUPER_MOVIE_NAME = "a super movie";
    static final String OTHER_SUPER_MOVIE_NAME = "another super movie";

    Movie createSmallFishMovie() {
        return createSmallFishMovie(LocalDate.of(2023, 10, 10));
    }

    Buyer createUserNicolas() {
        return new Buyer(1L);
    }

    Movie createSmallFishMovie(LocalDate releaseDate) {
        return new Movie(1L, "Small Fish", 102,
                releaseDate,
                Set.of("COMEDY", "ACTION")/* genre */);
    }

    PaymenentProviderFake fakePaymenentProvider() {
        return new PaymenentProviderFake();
    }

    PaymenentProviderThrowException fakePaymenentProviderThrowE() {
        return new PaymenentProviderThrowException();
    }

    Long createSuperMovie(Shows shows) {
        return shows.addNewMovie(1L, SUPER_MOVIE_NAME, 109,
                LocalDate.of(2023, 4, 5),
                Set.of("ACTION", "ADVENTURE"));
    }

    CreditCardPaymentProvider doNothingPaymentProvider() {
        return (creditCardNumber, expire, securityCode, totalAmount) -> {
        };
    }


    ShowTime createShowForSmallFish() {
        return new ShowTime(DateTimeProvider.create(), createSmallFishMovie(),
                LocalDateTime.now().plusDays(1), 10f,
                new Theater("a Theater", Set.of(1, 2, 3, 4, 5, 6)));
    }
}

class PaymenentProviderFake implements CreditCardPaymentProvider {
    private String creditCardNumber;
    private YearMonth expire;
    private String securityCode;
    private float totalAmount;

    @Override
    public void pay(String creditCardNumber, YearMonth expire,
                    String securityCode, float totalAmount) {
        this.creditCardNumber = creditCardNumber;
        this.expire = expire;
        this.securityCode = securityCode;
        this.totalAmount = totalAmount;
    }

    public boolean hasBeanCalledWith(String creditCardNumber, YearMonth expire,
                                     String securityCode, float totalAmount) {
        return this.creditCardNumber.equals(creditCardNumber)
                && this.expire.equals(expire)
                && this.securityCode.equals(securityCode)
                && this.totalAmount == totalAmount;
    }

}

class PaymenentProviderThrowException implements CreditCardPaymentProvider {
    @Override
    public void pay(String creditCardNumber, YearMonth expire,
                    String securityCode, float totalAmount) {
        throw new RuntimeException("very bad...");
    }
}

