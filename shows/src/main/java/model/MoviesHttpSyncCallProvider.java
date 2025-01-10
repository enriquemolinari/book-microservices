package model;

import api.ShowsException;
import common.NotBlankString;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MoviesHttpSyncCallProvider implements MovieInfoProvider {

    public static final String NOT_AVAILABLE_MSG = "Not Available";
    public static final String VALID_URL_REQUIRED_ERROR_MSG = "A valid Url is required";
    public static final String MOVIE_IDS_NOT_BE_EMPTY = "Movie ids must not be empty";
    public static final String MOVIE_URI_ENDS_WITH = "Movie Uri must end with %s to replace with ids";
    private static final int TIME_OUT_SECONDS = 2;
    private final String moviesEndpointUrl;

    public MoviesHttpSyncCallProvider(String moviesEndpointUrl) {
        this.moviesEndpointUrl = new NotBlankString(moviesEndpointUrl
                , new ShowsException(VALID_URL_REQUIRED_ERROR_MSG)).value();
        checkUriEndsWith(moviesEndpointUrl);
    }

    @Override
    public Map<Long, MovieInfo> moviesBy(List<Long> ids) {
        checkMovieIdsNotEmpty(ids);
        try {
            var moviesUri = new URI(moviesEndpointUrl.formatted(toCommaSeparated(ids)));
            HttpRequest request = HttpRequest.newBuilder(moviesUri)
                    .timeout(Duration.ofSeconds(TIME_OUT_SECONDS))
                    .build();
            HttpResponse<String> response;
            try (var httpClient = HttpClient.newHttpClient()) {
                response = httpClient.send(request
                        , HttpResponse.BodyHandlers.ofString());
                return new MovieInfoJsonToMap(response.body()).convert();
            }
        } catch (Throwable e) {
            // It is crucial to raise a FATAL error in the frequently queried log stream here,
            // ensuring that operations are promptly notified.
            return fallback(ids);
        }
    }

    private Map<Long, MovieInfo> fallback(List<Long> ids) {
        return ids.stream()
                .collect(Collectors.toMap(id -> id,
                        id -> new MovieInfo(id, NOT_AVAILABLE_MSG, NOT_AVAILABLE_MSG, Set.of())));
    }

    private void checkMovieIdsNotEmpty(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ShowsException(MOVIE_IDS_NOT_BE_EMPTY);
        }
    }

    private void checkUriEndsWith(String moviesEndpointUrl) {
        if (!moviesEndpointUrl.endsWith("%s")) {
            throw new ShowsException(MOVIE_URI_ENDS_WITH);
        }
    }

    private String toCommaSeparated(List<Long> ids) {
        return ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}
