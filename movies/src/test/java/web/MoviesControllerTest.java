package web;

import io.restassured.http.Header;
import io.restassured.response.Response;
import main.Main;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static web.MoviesController.FW_GATEWAY_USER_ID;

@SpringBootTest(classes = Main.class, webEnvironment = WebEnvironment.DEFINED_PORT)
// Note: This test starts with a sample database and retains it until all tests are completed.
// This approach may introduce issues since the database is shared across all tests.
// see AppTestConfiguration
@ActiveProfiles(value = "test")
public class MoviesControllerTest {

    public static final String NICO_USER_ID = "2";
    public static final String USER_ID_KEY = "userId";
    private static final String COMMENT_KEY = "comment";
    private static final String RATE_VALUE_KEY = "rateValue";
    private static final String JSON_ROOT = "$";
    private static final String MOVIE_NAME_KEY = "name";
    private static final String MOVIE_ACTORS_KEY = "actors";
    private static final String MOVIE_RATING_TOTAL_VOTES_KEY = "ratingTotalVotes";
    private static final String MOVIE_RATING_VALUE_KEY = "ratingValue";
    private static final String MOVIE_RELEASE_DATE_KEY = "releaseDate";
    private static final String MOVIE_DIRECTORS_KEY = "directorNames";
    private static final String MOVIE_DURATION_KEY = "duration";
    private static final String MOVIE_PLOT_KEY = "plot";
    private static final String MOVIE_GENRES_KEY = "genres";
    private static final String ROCK_IN_THE_SCHOOL_MOVIE_NAME = "Rock in the School";
    private static final String RUNNING_FAR_AWAY_MOVIE_NAME = "Running far Away";
    private static final String SMALL_FISH_MOVIE_NAME = "Small Fish";
    private static final String CRASH_TEA_MOVIE_NAME = "Crash Tea";
    private static final String ERROR_MESSAGE_KEY = "message";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String HOST = "http://localhost";
    @Value("${server.port}")
    private String SERVER_PORT;

    private String urlForTests() {
        return HOST.concat(":").concat(SERVER_PORT);
    }

    @Test
    public void moviesOk() {
        var response = get(urlForTests() + "/movies");

        response.then().body(MOVIE_NAME_KEY,
                hasItems(CRASH_TEA_MOVIE_NAME,
                        ROCK_IN_THE_SCHOOL_MOVIE_NAME));

        assertOnMovies(response);
    }

    @Test
    public void moviesInfoOk() {
        var response = get(urlForTests() + "/movies/by/1,2,3,4");

        response.then().body(MOVIE_NAME_KEY,
                hasItems(CRASH_TEA_MOVIE_NAME,
                        ROCK_IN_THE_SCHOOL_MOVIE_NAME,
                        SMALL_FISH_MOVIE_NAME,
                        RUNNING_FAR_AWAY_MOVIE_NAME));
        response.then().body(JSON_ROOT, hasItem(hasKey(MOVIE_GENRES_KEY)));
        response.then().body(JSON_ROOT, hasItem(hasKey(MOVIE_DURATION_KEY)));
    }

    @Test
    public void moviesSortedRateOk() {
        var response = get(urlForTests() + "/movies/sorted/rate");
        response.then().body("[0].name", is(ROCK_IN_THE_SCHOOL_MOVIE_NAME));
        response.then().body("[1].name", is(SMALL_FISH_MOVIE_NAME));
        assertOnMovies(response);
    }

    @Test
    public void moviesSortedReleaseDateOk() {
        var response = get(urlForTests() + "/movies/sorted/releasedate");
        response.then().body("[0].name", is(RUNNING_FAR_AWAY_MOVIE_NAME));
        response.then().body("[1].name", is(ROCK_IN_THE_SCHOOL_MOVIE_NAME));
        assertOnMovies(response);
    }

    @Test
    public void moviesSearchOk() {
        var response = get(urlForTests() + "/movies/search/rock");
        response.then().body("[0].name", is(ROCK_IN_THE_SCHOOL_MOVIE_NAME));
        assertOnMovies(response);
    }


    private void assertOnMovies(Response response) {
        response.then().body(JSON_ROOT, hasItem(hasKey(MOVIE_GENRES_KEY)));
        response.then().body(JSON_ROOT, hasItem(hasKey(MOVIE_PLOT_KEY)));
        response.then().body(JSON_ROOT, hasItem(hasKey(MOVIE_DURATION_KEY)));
        response.then().body(JSON_ROOT, hasItem(hasKey(MOVIE_DIRECTORS_KEY)));
        response.then().body(JSON_ROOT,
                hasItem(hasKey(MOVIE_RELEASE_DATE_KEY)));
        response.then().body(JSON_ROOT,
                hasItem(hasKey(MOVIE_RATING_VALUE_KEY)));
        response.then().body(JSON_ROOT,
                hasItem(hasKey(MOVIE_RATING_TOTAL_VOTES_KEY)));
        response.then().body(JSON_ROOT, hasItem(hasKey(MOVIE_ACTORS_KEY)));
    }

    @Test
    public void movieOneOk() {
        var response = get(urlForTests() + "/movies/1");

        response.then().body(MOVIE_NAME_KEY,
                is(oneOf(SMALL_FISH_MOVIE_NAME, ROCK_IN_THE_SCHOOL_MOVIE_NAME,
                        RUNNING_FAR_AWAY_MOVIE_NAME, CRASH_TEA_MOVIE_NAME)));

        response.then().body(JSON_ROOT, hasKey(MOVIE_GENRES_KEY));
        response.then().body(JSON_ROOT, hasKey(MOVIE_PLOT_KEY));
        response.then().body(JSON_ROOT, hasKey(MOVIE_DURATION_KEY));
        response.then().body(JSON_ROOT, hasKey(MOVIE_DIRECTORS_KEY));
        response.then().body(JSON_ROOT, hasKey(MOVIE_RELEASE_DATE_KEY));
        response.then().body(JSON_ROOT, hasKey(MOVIE_RATING_VALUE_KEY));
        response.then().body(JSON_ROOT, hasKey(MOVIE_RATING_TOTAL_VOTES_KEY));
        response.then().body(JSON_ROOT, hasKey(MOVIE_ACTORS_KEY));
    }

    @Test
    public void ratesFromMovieOneOk() {
        var response = get(urlForTests() + "/movies/1/rate");

        response.then().body(JSON_ROOT,
                hasItem(allOf(both(hasEntry(USER_ID_KEY, 3)).and(
                                (hasEntry(COMMENT_KEY,
                                        "I really enjoy the movie")))
                        .and(hasEntry(RATE_VALUE_KEY, 4)))));

        response.then().body(JSON_ROOT,
                hasItem(allOf(both(hasEntry(USER_ID_KEY, Integer.valueOf(NICO_USER_ID))).and(
                                (hasEntry(COMMENT_KEY,
                                        "Fantastic! The actors, the music, everything is fantastic!")))
                        .and(hasEntry(RATE_VALUE_KEY, 5)))));
    }

    @Test
    public void rateMovieOk() throws JSONException {
        JSONObject rateRequestBody = new JSONObject();
        rateRequestBody.put(RATE_VALUE_KEY, 4);
        rateRequestBody.put(COMMENT_KEY, "a comment...");

        var response = given().contentType(JSON_CONTENT_TYPE)
                .header(new Header(FW_GATEWAY_USER_ID, NICO_USER_ID))
                .body(rateRequestBody.toString())
                .post(urlForTests() + "/movies/private/" + NICO_USER_ID + "/rate");

        response.then().body(USER_ID_KEY, is(Integer.valueOf(NICO_USER_ID)))
                .body(RATE_VALUE_KEY, is(4))
                .body(COMMENT_KEY, is("a comment..."));
    }

    @Test
    public void rateMovieFailIfNotAuthenticated() throws JSONException {
        JSONObject rateRequestBody = new JSONObject();
        rateRequestBody.put(RATE_VALUE_KEY, 4);
        rateRequestBody.put(COMMENT_KEY, "a comment...");

        var response = given().contentType(JSON_CONTENT_TYPE)
                .body(rateRequestBody.toString())
                .post(urlForTests() + "/movies/private/1/rate");

        response.then().statusCode(401).body(ERROR_MESSAGE_KEY,
                is(MoviesController.AUTHENTICATION_REQUIRED));
    }
}