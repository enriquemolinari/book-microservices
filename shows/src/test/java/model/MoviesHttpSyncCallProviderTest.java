package model;

import api.ShowsException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockserver.integration.ClientAndServer;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static model.MoviesHttpSyncCallProvider.NOT_AVAILABLE_MSG;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class MoviesHttpSyncCallProviderTest {

    public static final String VALID_MOVIES_URL = "http://localhost:1080/movies/by/%s";
    private ClientAndServer mockServer;

    private static MoviesHttpSyncCallProvider buildMoviesProvider() {
        return new MoviesHttpSyncCallProvider(
                VALID_MOVIES_URL);
    }

    static Stream<List<String>> nullAndEmptyList() {
        return Stream.of(
                null,          // Valor null
                List.of()      // Lista vacía
        );
    }

    @BeforeEach
    public void setUp() {
        mockServer = ClientAndServer.startClientAndServer(1080);
    }

    @AfterEach
    public void tearDown() {
        mockServer.stop();
    }

    @Test
    public void syncCallTimesout() {
        mockServer.when(request().withPath("/movies/by/2,3"))
                .respond(response().withDelay(TimeUnit.SECONDS, 3));
        var map = buildMoviesProvider().movies(List.of(2L, 3L));
        assertEquals(2, map.size());
        assertEquals(NOT_AVAILABLE_MSG, map.get(2L).name());
        assertEquals(NOT_AVAILABLE_MSG, map.get(3L).name());
        assertEquals(NOT_AVAILABLE_MSG, map.get(2L).duration());
        assertEquals(NOT_AVAILABLE_MSG, map.get(3L).duration());
        assertEquals(Set.of(), map.get(2L).genres());
        assertEquals(Set.of(), map.get(3L).genres());
    }

    @Test
    public void providerEmptyUrl() {
        var e = assertThrows(ShowsException.class
                , () -> new MoviesHttpSyncCallProvider(""));
        assertEquals(MoviesHttpSyncCallProvider.VALID_URL_REQUIRED_ERROR_MSG, e.getMessage());
    }

    @Test
    public void providerNotEndingWith() {
        var e = assertThrows(ShowsException.class, () ->
                new MoviesHttpSyncCallProvider("http://localhost:8080/movies/by/")
        );
        assertEquals(MoviesHttpSyncCallProvider.MOVIE_URI_ENDS_WITH, e.getMessage());
    }

    @Test
    public void providerEndingWith() {
        var provider = new MoviesHttpSyncCallProvider(VALID_MOVIES_URL);
        assertNotNull(provider);
    }

    @ParameterizedTest
    @MethodSource("nullAndEmptyList")
    public void tryToSyncCallWithEmptyMovieIds(List<Long> ids) {
        var provider = new MoviesHttpSyncCallProvider(VALID_MOVIES_URL);
        var e = assertThrows(ShowsException.class, () -> provider.movies(ids));
        assertEquals(MoviesHttpSyncCallProvider.MOVIE_IDS_NOT_BE_EMPTY, e.getMessage());
    }

    @Test
    public void syncCallOk() {
        mockServer.when(request().withPath("/movies/by/1,2"))
                .respond(response().withBody(jsonBodyValid()));
        var provider = buildMoviesProvider();
        var map = provider.movies(List.of(1L, 2L));
        assertEquals(2, map.size());
        assertEquals("Crash Tea", map.get(3L).name());
        assertEquals("Small Fish", map.get(2L).name());
        assertEquals("1hr 45mins", map.get(3L).duration());
        assertEquals("2hrs 05mins", map.get(2L).duration());
        assertEquals(1, map.get(3L).genres().size());
        assertEquals(2, map.get(2L).genres().size());
        assertTrue(map.get(3L).genres().contains("Comedy"));
        assertTrue(map.get(2L).genres().contains("Drama"));
        assertTrue(map.get(2L).genres().contains("Adventure"));
    }

    private String jsonBodyValid() {
        return """
                [
                    {
                        "id": 3,
                        "name": "Crash Tea",
                        "duration": "1hr 45mins",
                        "genres": [
                            "Comedy"
                        ]
                    },
                    {
                        "id": 2,
                        "name": "Small Fish",
                        "duration": "2hrs 05mins",
                        "genres": [
                            "Adventure",
                            "Drama"
                        ]
                    }
                ]
                """;
    }
}
