package model;

import java.time.YearMonth;

public interface CreditCardPaymentProvider {
    void pay(String creditCardNumber, YearMonth expire, String securityCode,
             float totalAmount);
}
