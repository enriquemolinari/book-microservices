package web;

import api.MovieInfo;
import api.MoviesAuthException;
import api.MoviesSubSystem;
import api.UserMovieRate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Function;

@RestController
public class MoviesController {

    public static final String AUTHENTICATION_REQUIRED = "You must be logged in to perform this action...";
    public static final String FW_GATEWAY_USER_ID = "fw-gateway-user-id";

    private final MoviesSubSystem moviesSubSystem;

    public MoviesController(MoviesSubSystem moviesSubSystem) {
        this.moviesSubSystem = moviesSubSystem;
    }

    @GetMapping("/movies/{id}")
    public ResponseEntity<MovieInfo> movieDetail(@PathVariable Long id) {
        return ResponseEntity.ok(moviesSubSystem.movie(id));
    }

    @GetMapping("/movies/sorted/rate")
    public ResponseEntity<List<MovieInfo>> moviesSortedByRate(
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(moviesSubSystem.pagedMoviesSortedByRate(page));
    }

    @GetMapping("/movies/search/{fullOrPartialName}")
    public ResponseEntity<List<MovieInfo>> moviesSearchBy(
            @PathVariable String fullOrPartialName,
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity
                .ok(moviesSubSystem.pagedSearchMovieByName(fullOrPartialName, page));
    }

    @GetMapping("/movies/sorted/releasedate")
    public ResponseEntity<List<MovieInfo>> moviesSortedByReleaseDate(
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(moviesSubSystem.pagedMoviesSortedByReleaseDate(page));
    }

    @GetMapping("/movies")
    public ResponseEntity<List<MovieInfo>> allMovies(
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(moviesSubSystem.pagedMoviesSortedByName(page));
    }

    @GetMapping("/movies/{id}/rate")
    public ResponseEntity<List<UserMovieRate>> pagedRatesOfOrderedDate(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(moviesSubSystem.pagedRatesOfOrderedDate(id, page));
    }

    @PostMapping("/movies/private/{movieId}/rate")
    public ResponseEntity<UserMovieRate> rateMovie(
            @RequestHeader(value = FW_GATEWAY_USER_ID, required = false) Long userId,
            @PathVariable Long movieId, @RequestBody RateRequest rateRequest) {

        var userMovieRated = ifUserIdInHeaderDo(userId, (uid) -> {
            return this.moviesSubSystem.rateMovieBy(uid, movieId,
                    rateRequest.rateValue(), rateRequest.comment());
        });

        return ResponseEntity.ok(userMovieRated);
    }

    private <S> S ifUserIdInHeaderDo(Long userId, Function<Long, S> method) {
        if (userId == null) {
            throw new MoviesAuthException(AUTHENTICATION_REQUIRED);
        }
        return method.apply(userId);
    }
}
