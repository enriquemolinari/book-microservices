package model;

import common.DateTimeProvider;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Set;


public class ForTests {

    Buyer createUserNicolas() {
        return new Buyer(1L);
    }

    PaymenentProviderFake fakePaymenentProvider() {
        return new PaymenentProviderFake();
    }

    PaymenentProviderThrowException fakePaymenentProviderThrowE() {
        return new PaymenentProviderThrowException();
    }

    Long createAMovie(Shows shows, long id) {
        return shows.addNewMovie(id);
    }

    CreditCardPaymentProvider doNothingPaymentProvider() {
        return (creditCardNumber, expire, securityCode, totalAmount) -> {
        };
    }

    ShowTime createShowForSmallFish() {
        return new ShowTime(DateTimeProvider.create(), new Movie(1L),
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

