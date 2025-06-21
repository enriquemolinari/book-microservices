package web;

import io.restassured.response.Response;
import main.Main;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static web.ShowsController.FW_GATEWAY_USER_ID;

@SpringBootTest(classes = Main.class, webEnvironment = WebEnvironment.DEFINED_PORT)
// Note: This test starts with a sample database and retains it until all tests are completed.
// This approach may introduce issues since the database is shared across all tests.
// see AppTestConfiguration
@ActiveProfiles(value = "test")
public class ShowsControllerTest {

    public static final int SMALL_FISH_MOVIE_ID = 2;
    private static final String INFO_KEY = "info";
    private static final String SEAT_AVAILABLE_KEY = "available";
    private static final String CURRENT_SEATS_KEY = "currentSeats";
    private static final String JSON_ROOT = "$";
    private static final String SHOW_MOVIE_ID_KEY = "movieId";
    private static final String ERROR_MESSAGE_KEY = "message";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String HOST = "http://localhost";
    @Value("${server.port}")
    private String SERVER_PORT;

    private String urlForTests() {
        return HOST.concat(":").concat(SERVER_PORT);
    }

    @Test
    public void playingNowShowsOk() {
        var response = get(urlForTests() + "/shows");
        response.then().statusCode(200).body(SHOW_MOVIE_ID_KEY,
                hasItems(1, 2, 3, 4));
        response.then().body("find { it.movieId == 1 }.shows", hasSize(2));
        response.then().body("find { it.movieId == 2 }.shows", hasSize(2));
        response.then().body("find { it.movieId == 3 }.shows", hasSize(1));
        response.then().body("find { it.movieId == 4 }.shows", hasSize(1));
        response.then().body("find { it.movieId == 1 }.shows.find { it.showId == 3 }.price", equalTo(19.0f));
        response.then().body("find { it.movieId == 2 }.shows.find { it.showId == 1 }.price", equalTo(10.0f));
    }

    @Test
    public void showOneOk() {
        var response = get(urlForTests() + "/shows/1");
        response.then().body("info." + SHOW_MOVIE_ID_KEY, is(SMALL_FISH_MOVIE_ID));
        response.then().body("info.showId", is(1));
        response.then().body(JSON_ROOT, hasKey(CURRENT_SEATS_KEY));
        response.then().body(INFO_KEY, hasKey("playingTime"));
        response.then().body(INFO_KEY, hasKey("price"));
    }

    @Test
    public void buyerById() {
        var response = given()
                .header(FW_GATEWAY_USER_ID, "1")
                .get(urlForTests() + "/shows/buyer");
        response.then().body("buyerId", is(1));
        response.then().body("points", is(0));
    }

    @Test
    public void reserveAShowFailIfNotAuthenticated() {
        JSONArray seatsRequest = jsonBodyForReserveSeats(5, 7, 9);
        var response = given().contentType(JSON_CONTENT_TYPE)
                .body(seatsRequest.toString())
                .post(urlForTests() + "/shows/private/1/reserve");
        response.then().body(ERROR_MESSAGE_KEY,
                is(ShowsController.AUTHENTICATION_REQUIRED));
    }

    @Test
    public void testMovieShowsByMovieId() {
        var response = get(urlForTests() + "/shows/movie/" + SMALL_FISH_MOVIE_ID);
        response.then().body(SHOW_MOVIE_ID_KEY, is(SMALL_FISH_MOVIE_ID));
        response.then().body("shows", hasSize(2));
    }

    @Test
    public void reserveAlreadyReservedShowFail() {
        JSONArray seatsRequest = jsonBodyForReserveSeats(7);
        reservePost("2", seatsRequest, 1);

        JSONArray seatsRequest2 = jsonBodyForReserveSeats(5, 7, 9);
        var failedResponse = reservePost("1", seatsRequest2, 1);

        failedResponse.then().body(ERROR_MESSAGE_KEY,
                is("All or some of the seats chosen are busy"));
    }

    @Test
    public void payAShowFailIfNotAuthenticated() throws JSONException {
        JSONArray seatsRequest = jsonBodyForReserveSeats(SMALL_FISH_MOVIE_ID, 3, 7);

        JSONObject paymentRequest = paymentRequestForSeats(seatsRequest);

        var response = given().contentType(JSON_CONTENT_TYPE)
                .body(paymentRequest.toString())
                .post(urlForTests() + "/shows/private/1/pay");

        response.then().body(ERROR_MESSAGE_KEY,
                is(ShowsController.AUTHENTICATION_REQUIRED));
    }

    @Test
    public void payNotReservedSeatsFail() throws JSONException {
        JSONArray seatsRequest = jsonBodyForReserveSeats(SMALL_FISH_MOVIE_ID, 3, 7);

        JSONObject paymentRequest = paymentRequestForSeats(seatsRequest);

        var failedResponse = payShowOneForUserTwoPost(paymentRequest);

        failedResponse.then().body(ERROR_MESSAGE_KEY,
                is("Reservation is required before confirm"));
    }

    @Test
    public void saleInfoIsReturnedAfterPay() throws JSONException {
        JSONArray seatsRequest = jsonBodyForReserveSeats(10, 11);
        reservePost("2", seatsRequest, 1);
        JSONObject paymentRequest = paymentRequestForSeats(seatsRequest);
        var response = payShowOneForUserTwoPost(paymentRequest);
        var salesId = response.body().jsonPath().get("salesId");

        var salesResponse = given()
                .get(urlForTests() + "/shows/sale/" + salesId);
        salesResponse.then().body("salesIdentifier", is(salesId));
        salesResponse.then().body("pointsWon", is(10));
        salesResponse.then().body("userId", is(2));
        salesResponse.then().body("movieId", is(2));
        salesResponse.then().body("total", is(20.0F));
        salesResponse.then().body("$", hasKey("showStartTime"));
        salesResponse.then().body("seats", hasItems(11, 10));
    }

    @Test
    public void payReservedShowOk() throws JSONException {
        JSONArray seatsRequest = jsonBodyForReserveSeats(12, 13, 17);
        reservePost("2", seatsRequest, 1);

        JSONObject paymentRequest = paymentRequestForSeats(seatsRequest);

        var response = payShowOneForUserTwoPost(paymentRequest);

        response.then().body(SHOW_MOVIE_ID_KEY, is(SMALL_FISH_MOVIE_ID));
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

    @Test
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

        assertEquals(SMALL_FISH_MOVIE_ID, notAvailableSeats.size());
        assertEquals(28, availableSeats.size());

        response.then().body(INFO_KEY + "." + SHOW_MOVIE_ID_KEY, is(SMALL_FISH_MOVIE_ID));
        response.then().body("info.showId", is(2));
        response.then().body(JSON_ROOT, hasKey(CURRENT_SEATS_KEY));
        response.then().body(JSON_ROOT, hasKey("theater"));
        response.then().body(INFO_KEY, hasKey("playingTime"));
        response.then().body(INFO_KEY, hasKey("price"));
    }

    private Response reserveSeatsTwoFourFromShowTwoPost() {
        JSONArray seatsRequest = jsonBodyForReserveSeats(SMALL_FISH_MOVIE_ID, 4);

        return reservePost("1", seatsRequest, SMALL_FISH_MOVIE_ID);
    }

    private Response payShowOneForUserTwoPost(JSONObject paymentRequest) {
        return given().contentType(JSON_CONTENT_TYPE)
                .header(FW_GATEWAY_USER_ID, "2")
                .body(paymentRequest.toString())
                .post(urlForTests() + "/shows/private/" + 1 + "/pay");
    }

    private Response reservePost(String userId, JSONArray seatsRequest, int showId) {
        return given().contentType(JSON_CONTENT_TYPE)
                .header(FW_GATEWAY_USER_ID, userId)
                .body(seatsRequest.toString())
                .post(urlForTests() + "/shows/private/" + showId + "/reserve");
    }

    private JSONArray jsonBodyForReserveSeats(Integer... seats) {
        JSONArray seatsRequest = new JSONArray();
        for (Integer seat : seats) {
            seatsRequest.put(seat);
        }
        return seatsRequest;
    }
}