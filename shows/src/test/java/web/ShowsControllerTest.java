package web;

import api.ShowsSubSystem;
import io.restassured.response.Response;
import main.Main;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = Main.class, webEnvironment = WebEnvironment.DEFINED_PORT)
// Note: This test starts with a sample database and retains it until all tests are completed.
// This approach may introduce issues since the database is shared across all tests.
// see AppTestConfiguration
@ActiveProfiles(value = "test")
public class ShowsControllerTest {

    private static final String INFO_KEY = "info";
    private static final String SEAT_AVAILABLE_KEY = "available";
    private static final String CURRENT_SEATS_KEY = "currentSeats";
    private static final String PASSWORD_KEY = "password";
    private static final String USERNAME_KEY = "username";
    private static final String JSON_ROOT = "$";
    private static final String MOVIE_DURATION_KEY = "duration";
    private static final String SHOW_MOVIE_NAME_KEY = "movieName";
    private static final String ROCK_IN_THE_SCHOOL_MOVIE_NAME = "Rock in the School";
    private static final String RUNNING_FAR_AWAY_MOVIE_NAME = "Running far Away";
    private static final String SMALL_FISH_MOVIE_NAME = "Small Fish";
    private static final String CRASH_TEA_MOVIE_NAME = "Crash Tea";
    private static final String PASSWORD_JOSE = "123456789012";
    private static final String USERNAME_JOSE = "jsimini";
    private static final String ERROR_MESSAGE_KEY = "message";
    private static final String TOKEN_COOKIE_NAME = "token";
    private static final String JSON_CONTENT_TYPE = "application/json";
    //TODO: remove hardcoded port
    private static final String URL = "http://localhost:8081";
    @Autowired
    private ShowsSubSystem showsSubSystem;


    private Response loginAsJosePost() {
        return loginAsPost(USERNAME_JOSE, PASSWORD_JOSE);
    }

    private Response loginAsNicoPost() {
        return loginAsPost("nico", "123456789012");
    }

    private Response loginAsLuciaPost() {
        return loginAsPost("lucia", "123456789012");
    }

    private Response loginAsPost(String userName, String password) {
        JSONObject loginRequestBody = new JSONObject();
        try {
            loginRequestBody.put(USERNAME_KEY, userName);
            loginRequestBody.put(PASSWORD_KEY, password);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return given().contentType(JSON_CONTENT_TYPE)
                .body(loginRequestBody.toString())
                .post(URL + "/login");
    }


    //@Test
    //TODO: re-write
    public void aPublishedTicketSoldChangePointsWonInUserProfile() {
        this.showsSubSystem.reserve(1L, 3L, Set.of(2, 3, 5, 6));
        this.showsSubSystem.pay(1L,
                3L,
                Set.of(2, 3),
                "numero",
                YearMonth.of(YearMonth.now().getYear(), YearMonth.now().getMonth()),
                "code1");
        this.showsSubSystem.pay(1L,
                3L,
                Set.of(5, 6),
                "numero",
                YearMonth.of(YearMonth.now().getYear(), YearMonth.now().getMonth()),
                "code1");
//        var profile = this.usersSubSystem.profileFrom(1l);
//        assertEquals(20, profile.points());
    }

    //@Test
    //TODO: re-write
    public void aPublishedTicketSoldPushANotificationJob() {
        Set<Integer> selectedSeats = Set.of(12, 13);
        this.showsSubSystem.reserve(1L, 3L, selectedSeats);
        this.showsSubSystem.pay(1L,
                3L,
                selectedSeats,
                "numero",
                YearMonth.of(YearMonth.now().getYear(), YearMonth.now().getMonth()),
                "code1");
//        List<String[]> jobs = this.notificationsSubSystem.allJobs();
//        var showInfo = this.showsSubSystem.show(3L);
//        int lastJob = jobs.size() - 1;
//        JsonPath jsonPath = new JsonPath(jobs.get(lastJob)[1]);
//        assertEquals(1, jsonPath.getInt("idUser"));
//        assertTrue(selectedSeats.containsAll(jsonPath.getList("payedSeats")));
//        assertTrue(jsonPath.getList("payedSeats").containsAll(selectedSeats));
//        assertEquals(10, jsonPath.getInt("pointsWon"));
//        assertEquals(showInfo.info().playingTime(), jsonPath.getString("showStartTime"));
    }

    //@Test
    //TODO: re-write once security is in place
    public void aPublishedRegisteredUserItIsAllowedToReserve() throws JSONException {
        JSONObject registerRequestBody = new JSONObject();
        registerRequestBody.put("name", "apublisheduser");
        registerRequestBody.put("surname", "ausersurname");
        registerRequestBody.put("email", "auser2@ma.com");
        registerRequestBody.put(USERNAME_KEY, "apublishedusername");
        registerRequestBody.put(PASSWORD_KEY, "444467890124");
        registerRequestBody.put("repeatPassword", "444467890124");

        given().contentType(JSON_CONTENT_TYPE)
                .body(registerRequestBody.toString())
                .post(URL + "/users/register");

//        var loginResponse = loginAsPost("apublishedusername", "444467890124");
//        var token = getCookie(loginResponse);
//        JSONArray seatsRequest = jsonBodyForReserveSeats(29);
//        var response = reservePost(token, seatsRequest, 1);
//        response.then().statusCode(200);
    }

    @Test
    public void playingNowShowsOk() {
        var response = get(URL + "/shows");

        response.then().body(SHOW_MOVIE_NAME_KEY,
                hasItems(CRASH_TEA_MOVIE_NAME, SMALL_FISH_MOVIE_NAME,
                        ROCK_IN_THE_SCHOOL_MOVIE_NAME,
                        RUNNING_FAR_AWAY_MOVIE_NAME));

        response.then().body(MOVIE_DURATION_KEY,
                hasItems("1hr 49mins", "2hrs 05mins", "1hr 45mins",
                        "1hr 45mins"));
    }

    @Test
    public void showOneOk() {
        var response = get(URL + "/shows/1");
        // To avoid fragile tests, I use oneOf, as the movie assigned to show 1
        // might change
        response.then().body("info." + SHOW_MOVIE_NAME_KEY,
                is(oneOf(SMALL_FISH_MOVIE_NAME, ROCK_IN_THE_SCHOOL_MOVIE_NAME,
                        RUNNING_FAR_AWAY_MOVIE_NAME, CRASH_TEA_MOVIE_NAME)));
        response.then().body("info.showId", is(1));
        response.then().body(JSON_ROOT, hasKey(CURRENT_SEATS_KEY));
        response.then().body(INFO_KEY, hasKey("movieDuration"));
    }

    //@Test
    //TODO: re-write once security is in place
    public void reserveAShowFailIfNotAuthenticated() {
        JSONArray seatsRequest = jsonBodyForReserveSeats(5, 7, 9);
        var response = given().contentType(JSON_CONTENT_TYPE)
                .body(seatsRequest.toString())
                .post(URL + "/shows/1/reserve");
        response.then().body(ERROR_MESSAGE_KEY,
                is(ShowsController.AUTHENTICATION_REQUIRED));
    }

    //@Test
    //TODO: re-write once security is in place
    public void reserveAlreadyReservedShowFail() {
        var token = loginAsNicoAndGetCookie();
        JSONArray seatsRequest = jsonBodyForReserveSeats(7);
        reservePost(token, seatsRequest, 1);

        var tokenJose = loginAsJoseAndGetCookie();
        JSONArray seatsRequest2 = jsonBodyForReserveSeats(5, 7, 9);
        var failedResponse = reservePost(tokenJose, seatsRequest2, 1);

        failedResponse.then().body(ERROR_MESSAGE_KEY,
                is("All or some of the seats chosen are busy"));
    }

    //@Test
    //TODO: re-write once security is in place
    public void payAShowFailIfNotAuthenticated() throws JSONException {
        JSONArray seatsRequest = jsonBodyForReserveSeats(2, 3, 7);

        JSONObject paymentRequest = paymentRequestForSeats(seatsRequest);

        var response = given().contentType(JSON_CONTENT_TYPE)
                .body(paymentRequest.toString())
                .post(URL + "/shows/1/pay");

        response.then().body(ERROR_MESSAGE_KEY,
                is(ShowsController.AUTHENTICATION_REQUIRED));
    }

    //@Test
    //TODO: re-write once security is in place
    public void payNotReservedSeatsFail() throws JSONException {
        var token = loginAsNicoAndGetCookie();
        JSONArray seatsRequest = jsonBodyForReserveSeats(2, 3, 7);

        JSONObject paymentRequest = paymentRequestForSeats(seatsRequest);

        var failedResponse = payPost(token, paymentRequest, 1);

        failedResponse.then().body(ERROR_MESSAGE_KEY,
                is("Reservation is required before confirm"));
    }

    //@Test
    //TODO: re-write once security is in place
    public void payReservedShowOk() throws JSONException {
        var token = loginAsNicoAndGetCookie();
        JSONArray seatsRequest = jsonBodyForReserveSeats(12, 13, 17);
        reservePost(token, seatsRequest, 1);

        JSONObject paymentRequest = paymentRequestForSeats(seatsRequest);

        var response = payPost(token, paymentRequest, 1);

        response.then().body(SHOW_MOVIE_NAME_KEY,
                is(oneOf(SMALL_FISH_MOVIE_NAME, ROCK_IN_THE_SCHOOL_MOVIE_NAME,
                        RUNNING_FAR_AWAY_MOVIE_NAME, CRASH_TEA_MOVIE_NAME)));
        response.then().body("total", is(30.0F));
        response.then().body("pointsWon", is(10));
        response.then().body("payedSeats", hasItems(12, 13, 17));
    }

    private JSONObject paymentRequestForSeats(JSONArray seatsRequest)
            throws JSONException {
        JSONObject paymentRequest = new JSONObject();
        paymentRequest.put("selectedSeats", seatsRequest);
        paymentRequest.put("creditCardNumber", "56565-98758-2323");
        paymentRequest.put("secturityCode", "326");
        paymentRequest.put("expirationYear",
                LocalDate.now().plusYears(1).getYear());
        paymentRequest.put("expirationMonth", 11);
        return paymentRequest;
    }

    //@Test
    //TODO: re-write once security is in place
    public void reserveAShowOk() {
        var response = reserveSeatsTwoFourFromShowTwoPost();

        List<Map<String, Object>> list = response.then().extract().jsonPath()
                .getList(CURRENT_SEATS_KEY);

        var notAvailableSeats = list.stream()
                .filter(l -> l.get(SEAT_AVAILABLE_KEY).equals(false))
                .toList();
        var availableSeats = list.stream()
                .filter(l -> l.get(SEAT_AVAILABLE_KEY).equals(true))
                .toList();

        assertEquals(2, notAvailableSeats.size());
        assertEquals(28, availableSeats.size());

        response.then().body(INFO_KEY + "." + SHOW_MOVIE_NAME_KEY,
                is(oneOf(SMALL_FISH_MOVIE_NAME, ROCK_IN_THE_SCHOOL_MOVIE_NAME,
                        RUNNING_FAR_AWAY_MOVIE_NAME, CRASH_TEA_MOVIE_NAME)));
        response.then().body("info.showId", is(2));
        response.then().body(JSON_ROOT, hasKey(CURRENT_SEATS_KEY));
        response.then().body(INFO_KEY, hasKey("movieDuration"));
    }

    private Response reserveSeatsTwoFourFromShowTwoPost() {
        var token = loginAsJoseAndGetCookie();

        JSONArray seatsRequest = jsonBodyForReserveSeats(2, 4);

        return reservePost(token, seatsRequest, 2);
    }

    private Response payPost(String token, JSONObject paymentRequest, int showId) {
        return given().contentType(JSON_CONTENT_TYPE)
                .cookie(TOKEN_COOKIE_NAME, token)
                .body(paymentRequest.toString())
                .post(URL + "/shows/" + showId + "/pay");
    }

    private Response reservePost(String token, JSONArray seatsRequest, int showId) {
        return given().contentType(JSON_CONTENT_TYPE)
                .cookie(TOKEN_COOKIE_NAME, token)
                .body(seatsRequest.toString())
                .post(URL + "/shows/" + showId + "/reserve");
    }

    private JSONArray jsonBodyForReserveSeats(Integer... seats) {
        JSONArray seatsRequest = new JSONArray();
        for (Integer seat : seats) {
            seatsRequest.put(seat);
        }
        return seatsRequest;
    }

    private String loginAsLuciaAndGetCookie() {
        var loginResponse = loginAsLuciaPost();
        return getCookie(loginResponse);
    }

    private String loginAsNicoAndGetCookie() {
        var loginResponse = loginAsNicoPost();
        return getCookie(loginResponse);
    }

    private String getCookie(Response loginResponse) {
        return loginResponse.getCookie(TOKEN_COOKIE_NAME);
    }

    private String loginAsJoseAndGetCookie() {
        var loginResponse = loginAsJosePost();
        return getCookie(loginResponse);
    }
}