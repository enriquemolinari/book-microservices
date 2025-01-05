package web;

import api.AuthException;
import api.UserProfile;
import api.UsersSubSystem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.function.Function;

@RestController
public class UsersController {

    public static final String AUTHENTICATION_REQUIRED = "You must be logged in to perform this action...";
    public static final String FW_GATEWAY_USER_ID = "fw-gateway-user-id";
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

    @GetMapping("/users/private/profile")
    public ResponseEntity<UserProfile> userProfile(
            // Although this request header is required, I don't want to define it that way
            // because Spring would respond with an error message, and I prefer not to expose
            // such details to end users for security reasons.
            @RequestHeader(value = FW_GATEWAY_USER_ID, required = false) Long userId) {
        return ifUserIdInHeaderDo(userId, uid -> {
            var profile = usersSubSystem.profileFrom(uid);
            return ResponseEntity.ok(profile);
        });
    }

    @PostMapping("/users/private/changepassword")
    public ResponseEntity<Void> changePassword(
            @RequestHeader(value = FW_GATEWAY_USER_ID, required = false) Long userId,
            @RequestBody ChangePasswordRequest passBody) {
        return ifUserIdInHeaderDo(userId, uid -> {
            usersSubSystem.changePassword(userId, passBody.currentPassword(),
                    passBody.newPassword1(), passBody.newPassword2());
            return ResponseEntity.ok().build();
        });
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

    @PostMapping("/users/private/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = FW_GATEWAY_USER_ID, required = false) Long userId) {
        return ifUserIdInHeaderDo(userId, (uid) -> {
            var cookie = ResponseCookie.from(TOKEN_COOKIE_NAME, null)
                    .httpOnly(true).maxAge(0).build();
            var headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
            return ResponseEntity.ok().headers(headers).build();
        });
    }

    @PostMapping("/users/token")
    public ResponseEntity<Long> userIdIfTokenValid(@RequestBody String token) {
        Long userId = usersSubSystem.userIdFrom(token);
        return ResponseEntity.ok(userId);
    }

    private <S> S ifUserIdInHeaderDo(Long userId, Function<Long, S> method) {
        if (userId == null) {
            throw new AuthException(AUTHENTICATION_REQUIRED);
        }
        return method.apply(userId);
    }
}
