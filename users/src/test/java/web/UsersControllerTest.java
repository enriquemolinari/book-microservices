package web;

import api.UsersSubSystem;
import io.restassured.response.Response;
import main.Main;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(classes = Main.class, webEnvironment = WebEnvironment.DEFINED_PORT)
// Note: This test starts with a sample database and retains it until all tests are completed.
// This approach may introduce issues since the database is shared across all tests.
// see AppTestConfiguration
@ActiveProfiles(value = "test")
public class UsersControllerTest {

    public static final String LOGIN_URI = "/users/login";
    private static final String CHANGE_PASS_BODY_CURRENT_PASS = "currentPassword";
    private static final String CHANGE_PASS_BODY_PASSWORD1 = "newPassword1";
    private static final String CHANGE_PASS_BODY_PASSWORD2 = "newPassword2";
    private static final String PASSWORD_KEY = "password";
    private static final String JOSE_FULLNAME = "Josefina Simini";
    private static final String JOSE_EMAIL = "jsimini@mymovies.com";
    private static final String POINTS_KEY = "points";
    private static final String EMAIL_KEY = "email";
    private static final String FULLNAME_KEY = "fullname";
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_JOSE = "123456789012";
    private static final String USERNAME_JOSE = "jsimini";
    private static final String ERROR_MESSAGE_KEY = "message";
    private static final String TOKEN_COOKIE_NAME = "token";
    private static final String JSON_CONTENT_TYPE = "application/json";
    //TODO: remove hardcoded and port
    private static final String URL = "http://localhost:8081";
    private UsersSubSystem usersSubSystem;

    @Test
    public void loginOk() {
        var response = loginAsJosePost();

        response.then().body(FULLNAME_KEY, is(JOSE_FULLNAME))
                .body(USERNAME_KEY, is(USERNAME_JOSE))
                .body(EMAIL_KEY, is(JOSE_EMAIL))
                .body(POINTS_KEY, equalTo(0))
                .cookie(TOKEN_COOKIE_NAME, containsString("v2.local"));
    }

    @Test
    public void logoutOk() {
        var token = loginAsJoseAndGetCookie();

        var response = given().contentType(JSON_CONTENT_TYPE)
                .cookie(TOKEN_COOKIE_NAME, token)
                .post(URL + "/users/logout");

        var cookie = response.getDetailedCookie(TOKEN_COOKIE_NAME);
        assertEquals(0, cookie.getMaxAge());
        assertEquals("", cookie.getValue());
    }

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
                .post(URL + LOGIN_URI);
    }

    @Test
    public void loginFail() throws JSONException {
        JSONObject loginRequestBody = new JSONObject();
        loginRequestBody.put(USERNAME_KEY, USERNAME_JOSE);
        loginRequestBody.put(PASSWORD_KEY, "44446789012");

        var response = given().contentType(JSON_CONTENT_TYPE)
                .body(loginRequestBody.toString())
                .post(URL + LOGIN_URI);

        response.then().body(ERROR_MESSAGE_KEY,
                is("Invalid username or password"));
        assertFalse(response.cookies().containsKey(TOKEN_COOKIE_NAME));
    }

    @Test
    public void registerUserOk() throws JSONException {
        JSONObject registerRequestBody = new JSONObject();
        registerRequestBody.put("name", "auser");
        registerRequestBody.put("surname", "ausersurname");
        registerRequestBody.put("email", "auser@ma.com");
        registerRequestBody.put(USERNAME_KEY, "auniqueusername");
        registerRequestBody.put(PASSWORD_KEY, "444467890124");
        registerRequestBody.put("repeatPassword", "444467890124");

        var response = given().contentType(JSON_CONTENT_TYPE)
                .body(registerRequestBody.toString())
                .post(URL + "/users/register");

        response.then().statusCode(200);

        loginAsPost("auniqueusername", "444467890124").then()
                .cookie(TOKEN_COOKIE_NAME, containsString("v2.local"));
    }


//    @Test
//    public void aPublishedRegisteredUserItIsAllowedToBeNotified() {
//        var userId = this.usersSubSystem.registerUser("ausertopublish3",
//                "surname",
//                "auser3@unmail.com",
//                "ausertopublish3",
//                "444467890124",
//                "444467890124");
//        var user = this.notificationsSubSystem.userBy(userId);
//        assertEquals("ausertopublish3", user[1]);
//        assertEquals("auser3@unmail.com", user[2]);
//    }


    @Test
    public void retrieveUserProfileFailIfNotAuthenticated() {
        var response = get(URL + "/users/profile");

        response.then().body(ERROR_MESSAGE_KEY,
                is(UsersController.AUTHENTICATION_REQUIRED));
    }

    @Test
    public void changePasswordFailPasswordsDoesNotMatch() throws JSONException {
        var token = loginAsLuciaAndGetCookie();
        JSONObject changePassRequestBody = changePasswordBody();
        changePassRequestBody.put(CHANGE_PASS_BODY_PASSWORD2,
                "anotherpassword");
        var response = given().contentType(JSON_CONTENT_TYPE)
                .cookie(TOKEN_COOKIE_NAME, token)
                .body(changePassRequestBody.toString())
                .post(URL + "/users/changepassword");
        assertEquals(500, response.statusCode());
        response.then().body(ERROR_MESSAGE_KEY,
                is("Passwords must be equals"));
    }

    @Test
    public void changePasswordFailWhenNotAuthenticated() throws JSONException {
        JSONObject changePassRequestBody = changePasswordBody();

        var response = given().contentType(JSON_CONTENT_TYPE)
                .body(changePassRequestBody.toString())
                .post(URL + "/users/changepassword");

        response.then().body(ERROR_MESSAGE_KEY,
                is(UsersController.AUTHENTICATION_REQUIRED));
    }

    @Test
    public void changePasswordOk() throws JSONException {
        var token = loginAsLuciaAndGetCookie();

        JSONObject changePassRequestBody = changePasswordBody();

        var response = given().contentType(JSON_CONTENT_TYPE)
                .cookie(TOKEN_COOKIE_NAME, token)
                .body(changePassRequestBody.toString())
                .post(URL + "/users/changepassword");

        assertEquals(200, response.statusCode());
    }

    private JSONObject changePasswordBody()
            throws JSONException {
        JSONObject changePassRequestBody = new JSONObject();
        changePassRequestBody.put(CHANGE_PASS_BODY_CURRENT_PASS,
                "123456789012");
        changePassRequestBody.put(CHANGE_PASS_BODY_PASSWORD1,
                "9898989898989898");
        changePassRequestBody.put(CHANGE_PASS_BODY_PASSWORD2,
                "9898989898989898");
        return changePassRequestBody;
    }

    @Test
    public void retrieveUserProfileOk() {
        var token = loginAsJoseAndGetCookie();

        var response = given()
                .cookie(TOKEN_COOKIE_NAME, token)
                .get(URL + "/users/profile");

        response.then().body(USERNAME_KEY, is(USERNAME_JOSE))
                .body(FULLNAME_KEY, is(JOSE_FULLNAME))
                .body(POINTS_KEY, is(0))
                .body(EMAIL_KEY, is(JOSE_EMAIL));
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