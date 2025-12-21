package model;

import api.Genre;
import api.MoviesException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserRateTest {

    @Test
    public void rateValueMustBeGreaterOrEqualToZero() {
        var e = assertThrows(MoviesException.class, () -> {
            new UserRate(aUser(), -1, "comment", aMovie());
            fail("a UserRate must not be instantiated with a value less than 0");
        });
        assertEquals(UserRate.INVALID_RATING, e.getMessage());
    }

    @Test
    public void rateValueMustBeLessOrEqualToFive() {
        var e = assertThrows(MoviesException.class, () -> {
            new UserRate(aUser(), 6, "comment", aMovie());
            fail("a UserRate must not be instantiated with a value greater than 5");
        });
        assertEquals(UserRate.INVALID_RATING, e.getMessage());
    }

    @Test
    public void rateValueZeroIsValid() {
        var userRate = new UserRate(aUser(), 0, "comment", aMovie());
        assertNotNull(userRate);
    }

    @Test
    public void rateValueFiveIsValid() {
        var userRate = new UserRate(aUser(), 5, "comment", aMovie());
        assertNotNull(userRate);
    }

    @Test
    public void rateValueInMiddleRangeIsValid() {
        var userRate = new UserRate(aUser(), 3, "good movie", aMovie());
        assertNotNull(userRate);
    }

    private User aUser() {
        return new User(1L);
    }

    private Movie aMovie() {
        return new Movie("Test Movie", "A test plot", 120,
                LocalDate.now(), Set.of(Genre.ACTION));
    }
}
