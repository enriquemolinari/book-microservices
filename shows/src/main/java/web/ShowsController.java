package web;

import api.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@RestController
//Note: there is a convention with API Gateway team.
// Those endpoints that must be secured must be prefixed with /shows/private
public class ShowsController {

    public static final String AUTHENTICATION_REQUIRED = "You must be logged in to perform this action...";
    public static final String FW_GATEWAY_USER_ID = "fw-gateway-user-id";
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

    @PostMapping("/shows/private/{showId}/reserve")
    public ResponseEntity<DetailedShowInfo> makeReservation(
            @RequestHeader(value = FW_GATEWAY_USER_ID, required = false) Long userId,
            @PathVariable Long showId, @RequestBody Set<Integer> seats) {

        var showInfo = ifUserIdInHeaderDo(userId,
                uid -> this.showsSubSystem.reserve(uid, showId,
                        seats));

        return ResponseEntity.ok(showInfo);
    }

    @PostMapping("/shows/private/{showId}/pay")
    public ResponseEntity<Ticket> payment(
            @RequestHeader(value = FW_GATEWAY_USER_ID, required = false) Long userId,
            @PathVariable Long showId, @RequestBody PaymentRequest payment) {

        var ticket = ifUserIdInHeaderDo(userId, uid -> {
            return this.showsSubSystem.pay(uid, showId,
                    payment.selectedSeats(), payment.creditCardNumber(),
                    payment.toYearMonth(),
                    payment.secturityCode());
        });

        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/shows/buyer")
    public ResponseEntity<BuyerInfo> buyerBy(
            @RequestHeader(value = FW_GATEWAY_USER_ID, required = false) Long userId) {
        return ResponseEntity.ok(this.showsSubSystem.buyer(userId));
    }

    @GetMapping("/shows/sale/{salesIdentifier}")
    public ResponseEntity<SaleInfo> sale(@PathVariable String salesIdentifier) {
        return ResponseEntity.ok(this.showsSubSystem.sale(salesIdentifier));
    }

    private <S> S ifUserIdInHeaderDo(Long userId, Function<Long, S> method) {
        if (userId == null) {
            throw new ShowsAuthException(AUTHENTICATION_REQUIRED);
        }
        return method.apply(userId);
    }
}
