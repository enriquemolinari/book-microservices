package web;

import api.MoviesSubSystem;
import io.restassured.response.Response;
import main.Main;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = Main.class, webEnvironment = WebEnvironment.DEFINED_PORT)
// Note: This test starts with a sample database and retains it until all tests are completed.
// This approach may introduce issues since the database is shared across all tests.
// see AppTestConfiguration
@ActiveProfiles(value = "test")
public class MoviesControllerTest {

    private static final String PASSWORD_KEY = "password";
    private static final String COMMENT_KEY = "comment";
    private static final String RATE_VALUE_KEY = "rateValue";
    private static final String USERNAME_KEY = "username";
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
    private static final String SHOW_MOVIE_NAME_KEY = "movieName";
    private static final String ROCK_IN_THE_SCHOOL_MOVIE_NAME = "Rock in the School";
    private static final String RUNNING_FAR_AWAY_MOVIE_NAME = "Running far Away";
    private static final String SMALL_FISH_MOVIE_NAME = "Small Fish";
    private static final String CRASH_TEA_MOVIE_NAME = "Crash Tea";
    private static final String ERROR_MESSAGE_KEY = "message";
    private static final String TOKEN_COOKIE_NAME = "token";
    private static final String JSON_CONTENT_TYPE = "application/json";
    //TODO: remove hardcoded port
    private static final String URL = "http://localhost:8081";
    @Autowired
    private MoviesSubSystem moviesSubSystem;

    //@Test
    public void aPublishedRegisteredUserItIsAllowedToRankAMovie() {
        //TODO: como voy a testear esto? Generando un token a manopla?
//        var userId = this.usersSubSystem.registerUser("ausertopublish2",
//                "surname",
//                "auser@unmail.com",
//                "ausertopublish2",
//                "444467890124",
//                "444467890124");
//        var userRateMovie = this.moviesSubSystem.rateMovieBy(userId,
//                4L,
//                4,
//                "fantastic");
//        assertEquals("ausertopublish2", userRateMovie.username());
    }

    @Test
    public void moviesOk() {
        //RestAssured.port = 8091;
        var response = get(URL + "/movies");

        response.then().body(MOVIE_NAME_KEY,
                hasItems(CRASH_TEA_MOVIE_NAME,
                        ROCK_IN_THE_SCHOOL_MOVIE_NAME));

        assertOnMovies(response);
    }

    @Test
    public void moviesSortedRateOk() {
        var response = get(URL + "/movies/sorted/rate");
        response.then().body("[0].name", is(ROCK_IN_THE_SCHOOL_MOVIE_NAME));
        response.then().body("[1].name", is(SMALL_FISH_MOVIE_NAME));
        assertOnMovies(response);
    }

    @Test
    public void moviesSortedReleaseDateOk() {
        var response = get(URL + "/movies/sorted/releasedate");
        response.then().body("[0].name", is(RUNNING_FAR_AWAY_MOVIE_NAME));
        response.then().body("[1].name", is(ROCK_IN_THE_SCHOOL_MOVIE_NAME));
        assertOnMovies(response);
    }

    @Test
    public void moviesSearchOk() {
        var response = get(URL + "/movies/search/rock");
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
        var response = get(URL + "/movies/1");

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
        var response = get(URL + "/movies/1/rate");

        response.then().body(JSON_ROOT,
                hasItem(allOf(both(hasEntry(USERNAME_KEY, "lucia")).and(
                                (hasEntry(COMMENT_KEY,
                                        "I really enjoy the movie")))
                        .and(hasEntry(RATE_VALUE_KEY, 4)))));

        response.then().body(JSON_ROOT,
                hasItem(allOf(both(hasEntry(USERNAME_KEY, "nico")).and(
                                (hasEntry(COMMENT_KEY,
                                        "Fantastic! The actors, the music, everything is fantastic!")))
                        .and(hasEntry(RATE_VALUE_KEY, 5)))));
    }

    //@Test
    //TODO: como testeo esto?
    public void rateMovieOk() throws JSONException {
//        var token = loginAsJoseAndGetCookie();
//
//        JSONObject rateRequestBody = new JSONObject();
//        rateRequestBody.put(RATE_VALUE_KEY, 4);
//        rateRequestBody.put(COMMENT_KEY, "a comment...");
//
//        var response = given().contentType(JSON_CONTENT_TYPE)
//                .cookie(TOKEN_COOKIE_NAME, token)
//                .body(rateRequestBody.toString())
//                .post(URL + "/movies/2/rate");
//
//        response.then().body(USERNAME_KEY, is(USERNAME_JOSE))
//                .body(RATE_VALUE_KEY, is(4))
//                .body(COMMENT_KEY, is("a comment..."));
    }

    //@Test
    //TODO: ver que hago con esto...
    public void rateMovieFailIfNotAuthenticated() throws JSONException {
        JSONObject rateRequestBody = new JSONObject();
        rateRequestBody.put(RATE_VALUE_KEY, 4);
        rateRequestBody.put(COMMENT_KEY, "a comment...");

        var response = given().contentType(JSON_CONTENT_TYPE)
                .body(rateRequestBody.toString())
                .post(URL + "/movies/1/rate");

        response.then().body(ERROR_MESSAGE_KEY,
                is(MoviesController.AUTHENTICATION_REQUIRED));
    }

    private String getCookie(Response loginResponse) {
        return loginResponse.getCookie(TOKEN_COOKIE_NAME);
    }

}