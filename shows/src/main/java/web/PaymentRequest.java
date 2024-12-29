package web;

import java.time.YearMonth;
import java.util.Set;

public record PaymentRequest(Set<Integer> selectedSeats,
                             String creditCardNumber, String secturityCode,
                             int expirationYear, int expirationMonth) {

    YearMonth toYearMonth() {
        return YearMonth.of(expirationYear, expirationMonth);
    }

}
