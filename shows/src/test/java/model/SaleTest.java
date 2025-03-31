package model;

import api.ShowsException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SaleTest {
    private final ForTests tests = new ForTests();

    @Test
    public void saleCannotBeCreatedWithEmptySeats() {
        Exception e = assertThrows(ShowsException.class, () -> {
            var sale = Sale.registerNewSaleFor("id-1234", tests.createUserNicolas(),
                    100f,
                    10,
                    Set.of());
        });
        assertEquals(Sale.SALE_CANNOT_BE_CREATED_WITHOUT_SEATS, e.getMessage());
    }
}
