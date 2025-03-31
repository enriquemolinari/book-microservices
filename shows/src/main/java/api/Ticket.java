package api;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.List;
import java.util.Set;

@Getter(value = AccessLevel.PUBLIC)
public class Ticket {
    private final String salesId;
    private final float total;
    private final int pointsWon;
    private final String salesDate;
    private final List<Integer> payedSeats;
    private final Long movieId;
    private final String showStartTime;

    public Ticket(String salesId, float total, int pointsWon,
                  String formattedSalesDate, List<Integer> payedSeats, Long movieId, String showStartTime) {
        this.salesId = salesId;
        this.total = total;
        this.pointsWon = pointsWon;
        this.salesDate = formattedSalesDate;
        this.payedSeats = payedSeats;
        this.movieId = movieId;
        this.showStartTime = showStartTime;
    }

    public boolean hasSeats(Set<Integer> seats) {
        return this.payedSeats.containsAll(seats);
    }

    public float total() {
        return this.total;
    }

}
