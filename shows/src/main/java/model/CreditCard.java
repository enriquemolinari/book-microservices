package model;

import java.time.YearMonth;

public class CreditCard {
    private final String number;
    private final YearMonth expirationDate;
    private final String secturityCode;

    private CreditCard(String number, YearMonth expirationDate, String secturityCode) {
        checkValidNumber(number);
        checkValidExpirationDate(number);
        checkValidSecurityCode(number);
        this.number = number;
        this.expirationDate = expirationDate;
        this.secturityCode = secturityCode;
    }

    public static CreditCard of(String creditCardNumber, YearMonth expirationDate, String secturityCode) {
        return new CreditCard(creditCardNumber, expirationDate, secturityCode);
    }

    public String number() {
        return this.number;
    }

    public YearMonth expiration() {
        return this.expirationDate;
    }

    public String secturityCode() {
        return this.secturityCode;
    }

    private void checkValidSecurityCode(String number) {
        //do proper validation if not throw exception
    }

    private void checkValidExpirationDate(String number) {
        //do proper validation if not throw exception
    }

    private void checkValidNumber(String number) {
        //do proper validation if not throw exception
    }
}
