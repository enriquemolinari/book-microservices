package model;

import api.ShowsException;
import api.Ticket;

import java.util.Set;

public class Cashier {
    static final String CREDIT_CARD_DEBIT_HAS_FAILED = "Credit card debit have failed";
    static final String SELECTED_SEATS_SHOULD_NOT_BE_EMPTY = "Selected Seats should not be empty";
    private final SalesIdentifierGenerator idGenerator;
    private final CreditCardPaymentProvider paymentGateway;

    public Cashier(SalesIdentifierGenerator idGenerator, CreditCardPaymentProvider paymentGateway) {
        this.idGenerator = idGenerator;
        this.paymentGateway = paymentGateway;
    }

    public Ticket paySeatsFor(Set<Integer> selectedSeats,
                              ShowTime showTime,
                              Buyer buyer,
                              CreditCard creditCard) {
        checkSelectedSeats(selectedSeats);
        var total = showTime.totalAmountForTheseSeats(selectedSeats);
        try {
            // In this scenario, we have a service operation executed outside a Tx boundary.
            this.paymentGateway.pay(creditCard.number(), creditCard.expiration(),
                    creditCard.secturityCode(), total);
        } catch (Exception e) {
            throw new ShowsException(CREDIT_CARD_DEBIT_HAS_FAILED, e);
        }
        // If an exception occurs from now on, the transaction is rolled back.
        // It's imperative to ensure that the user is refunded promptly.
        // To handle this gracefully we should set up a compensation mechanism
        // not covered in this book
        var showSeats = showTime.confirmSeatsForUser(buyer, selectedSeats);
        return Sale.registerNewSaleFor(this.idGenerator.generate(), buyer, total, showTime.pointsToEarn(), showSeats);
    }

    private void checkSelectedSeats(Set<Integer> selectedSeats) {
        if (selectedSeats.isEmpty()) {
            throw new ShowsException(SELECTED_SEATS_SHOULD_NOT_BE_EMPTY);
        }
    }
}
