package api;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

public interface ShowsSubSystem {

    List<MovieShows> showsUntil(LocalDateTime untilTo);

    DetailedShowInfo show(Long id);

    Long addNewTheater(String name, Set<Integer> seatsNumbers);

    ShowInfo addNewShowFor(Long movieId, LocalDateTime startTime,
                           float price, Long theaterId, int pointsToWin);

    DetailedShowInfo reserve(Long userId, Long showTimeId,
                             Set<Integer> selectedSeats);

    Ticket pay(Long userId, Long showTimeId, Set<Integer> selectedSeats,
               String creditCardNumber, YearMonth expirationDate,
               String secturityCode);

    BuyerInfo buyer(Long userId);
}
