package api;

import java.util.List;

public record SaleInfo(String salesIdentifier, Long movieId, Long userId,
                       float total, int pointsWon,
                       List<Integer> seats, String showStartTime) {
}
