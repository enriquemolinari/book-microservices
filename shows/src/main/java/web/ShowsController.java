package web;

import api.DetailedShowInfo;
import api.MovieShows;
import api.ShowsSubSystem;
import api.Ticket;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@RestController
public class ShowsController {

    public static final String AUTHENTICATION_REQUIRED = "You must be logged in to perform this action...";
    private static final String TOKEN_COOKIE_NAME = "token";
    private final ShowsSubSystem showsSubSystem;

    public ShowsController(ShowsSubSystem showsSubSystem) {
        this.showsSubSystem = showsSubSystem;
    }

    @GetMapping("/shows")
    public List<MovieShows> playingTheseDays() {
        return showsSubSystem.showsUntil(LocalDateTime.now().plusDays(10));
    }

    @GetMapping("/shows/{id}")
    public ResponseEntity<DetailedShowInfo> showDetail(
            @PathVariable Long id) {
        return ResponseEntity.ok(showsSubSystem.show(id));
    }

    @PostMapping("/shows/{showId}/reserve")
    public ResponseEntity<DetailedShowInfo> makeReservation(
            @CookieValue(required = false) String token,
            @PathVariable Long showId, @RequestBody Set<Integer> seats) {

        var showInfo = ifAuthenticatedDo(token,
                userId -> this.showsSubSystem.reserve(userId, showId,
                        seats));

        return ResponseEntity.ok(showInfo);
    }

    @PostMapping("/shows/{showId}/pay")
    public ResponseEntity<Ticket> payment(
            @CookieValue(required = false) String token,
            @PathVariable Long showId, @RequestBody PaymentRequest payment) {

        var ticket = ifAuthenticatedDo(token, userId -> {
            return this.showsSubSystem.pay(userId, showId,
                    payment.selectedSeats(), payment.creditCardNumber(),
                    payment.toYearMonth(),
                    payment.secturityCode());
        });

        return ResponseEntity.ok(ticket);
    }


    private <S> S ifAuthenticatedDo(String token, Function<Long, S> method) {
        //TODO: fix this
        return null;
//        var userId = Optional.ofNullable(token).map(this.usersSubSystem::userIdFrom).
//                orElseThrow(() -> new AuthException(
//                        AUTHENTICATION_REQUIRED));
//
//        return method.apply(userId);
    }
}
