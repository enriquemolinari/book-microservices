package web;

import api.AuthException;
import api.UserProfile;
import api.UsersSubSystem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.function.Function;

@RestController
public class UsersController {

    public static final String AUTHENTICATION_REQUIRED = "You must be logged in to perform this action...";
    private static final String TOKEN_COOKIE_NAME = "token";
    private final UsersSubSystem usersSubSystem;

    public UsersController(UsersSubSystem usersSubSystem) {
        this.usersSubSystem = usersSubSystem;
    }


    @PostMapping("/users/register")
    public ResponseEntity<Long> userRegistration(
            @RequestBody UserRegistrationRequest request) {

        return ResponseEntity
                .ok(usersSubSystem.registerUser(request.name(), request.surname(),
                        request.email(),
                        request.username(), request.password(),
                        request.repeatPassword()));
    }

    @GetMapping("/users/profile")
    public ResponseEntity<UserProfile> userProfile(
            @CookieValue(required = false) String token) {
        var profile = ifAuthenticatedDo(token, usersSubSystem::profileFrom);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/users/changepassword")
    public ResponseEntity<Void> changePassword(
            @CookieValue(required = false) String token,
            @RequestBody ChangePasswordRequest passBody) {

        ifAuthenticatedDo(token, userId -> {
            usersSubSystem.changePassword(userId, passBody.currentPassword(),
                    passBody.newPassword1(), passBody.newPassword2());
            return null;
        });

        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/login")
    public ResponseEntity<UserProfile> login(@RequestBody LoginRequest form) {
        String token = usersSubSystem.login(form.username(), form.password());
        var profile = usersSubSystem.profileFrom(usersSubSystem.userIdFrom(token));

        var cookie = ResponseCookie.from(TOKEN_COOKIE_NAME, token)
                .httpOnly(true).path("/").build();
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok().headers(headers).body(profile);
    }

    @PostMapping("/users/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(required = false) String token) {
        return ifAuthenticatedDo(token, (userId) -> {
            var cookie = ResponseCookie.from(TOKEN_COOKIE_NAME, null)
                    .httpOnly(true).maxAge(0).build();
            var headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
            return ResponseEntity.ok().headers(headers).build();
        });
    }

    private <S> S ifAuthenticatedDo(String token, Function<Long, S> method) {
        var userId = Optional.ofNullable(token).map(this.usersSubSystem::userIdFrom).
                orElseThrow(() -> new AuthException(
                        AUTHENTICATION_REQUIRED));

        return method.apply(userId);
    }
}
