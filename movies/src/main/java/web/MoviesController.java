package web;

import api.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Function;

@RestController
//Note: there is a convention with API Gateway team.
// Those endpoints that must be secured must be prefixed with /movies/private
public class MoviesController {

    public static final String AUTHENTICATION_REQUIRED = "You must be logged in to perform this action...";
    public static final String FW_GATEWAY_USER_ID = "fw-gateway-user-id";

    private final MoviesSubSystem moviesSubSystem;

    public MoviesController(MoviesSubSystem moviesSubSystem) {
        this.moviesSubSystem = moviesSubSystem;
    }

    @GetMapping(Routes.MOVIES_BY_ID)
    public ResponseEntity<DetailedMovieInfo> movieDetail(@PathVariable Long id) {
        return ResponseEntity.ok(moviesSubSystem.movie(id));
    }

    @GetMapping(Routes.MOVIES_SORTED_RATE)
    public ResponseEntity<List<DetailedMovieInfo>> moviesSortedByRate(
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(moviesSubSystem.pagedMoviesSortedByRate(page));
    }

    @GetMapping(Routes.MOVIES_SEARCH)
    public ResponseEntity<List<DetailedMovieInfo>> moviesSearchBy(
            @PathVariable String fullOrPartialName,
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity
                .ok(moviesSubSystem.pagedSearchMovieByName(fullOrPartialName, page));
    }

    @GetMapping(Routes.MOVIES_SORTED_RELEASE_DATE)
    public ResponseEntity<List<DetailedMovieInfo>> moviesSortedByReleaseDate(
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(moviesSubSystem.pagedMoviesSortedByReleaseDate(page));
    }

    @GetMapping(Routes.MOVIES_LIST)
    public ResponseEntity<List<DetailedMovieInfo>> allMovies(
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(moviesSubSystem.pagedMoviesSortedByName(page));
    }

    @GetMapping(Routes.MOVIES_BY_IDS)
    public ResponseEntity<List<MovieInfo>> allMoviesInfo(
            @PathVariable List<Long> ids) {
        return ResponseEntity.ok(moviesSubSystem.allMovieInfosBy(ids));
    }

    @GetMapping(Routes.MOVIES_RATES)
    public ResponseEntity<List<UserMovieRate>> pagedRatesOfOrderedDate(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(moviesSubSystem.pagedRatesOfOrderedDate(id, page));
    }

    @PostMapping(Routes.MOVIES_PRIVATE_RATE)
    public ResponseEntity<UserMovieRate> rateMovie(
            @RequestHeader(value = FW_GATEWAY_USER_ID, required = false) Long userId,
            @PathVariable Long movieId, @RequestBody RateRequest rateRequest) {

        var userMovieRated = ifUserIdInHeaderDo(userId, (uid) -> {
            return this.moviesSubSystem.rateMovieBy(uid, movieId,
                    rateRequest.rateValue(), rateRequest.comment());
        });

        return ResponseEntity.ok(userMovieRated);
    }

    @PostMapping(Routes.MOVIES_PRIVATE_NEW)
    public ResponseEntity<DetailedMovieInfo> addNewMovie(
            @RequestHeader(value = FW_GATEWAY_USER_ID, required = false) Long userId,
            @RequestBody NewMovieRequest movieRequest) {

        var detailedMovieInfo = ifUserIdInHeaderDo(userId, (uid) -> {
            return this.moviesSubSystem.addNewMovie(movieRequest.name(), movieRequest.duration(),
                    movieRequest.releaseDate(), movieRequest.plot(), movieRequest.genres());
        });
        return ResponseEntity.ok(detailedMovieInfo);
    }

    private <S> S ifUserIdInHeaderDo(Long userId, Function<Long, S> method) {
        if (userId == null) {
            throw new MoviesAuthException(AUTHENTICATION_REQUIRED);
        }
        return method.apply(userId);
    }
}
