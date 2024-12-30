package model;

import api.UsersException;
import common.NotBlankString;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = {"password"})
class Password {
    static final String NOT_VALID_PASSWORD = "Password is not valid";
    static final String CAN_NOT_CHANGE_PASSWORD = "Some of the provided information is not valid to change the password";
    static final String PASSWORDS_MUST_BE_EQUALS = "Passwords must be equals";
    private String password;

    public Password(String password) {
        checkValidPassword(password);
        this.password = encript(password);
    }

    private void checkValidPassword(String password) {
        String pwd = new NotBlankString(password, new UsersException(NOT_VALID_PASSWORD)).value();
        if (pwd.length() < 12) {
            throw new UsersException(NOT_VALID_PASSWORD);
        }
    }

    private String encript(String nonEncriptedPassword) {
        // encript password here !
        return nonEncriptedPassword;
    }

    void checkPasswordsMatch(String password, String repeatPassword) {
        if (!password.equals(repeatPassword)) {
            throw new UsersException(PASSWORDS_MUST_BE_EQUALS);
        }
    }

    public void change(String currentPassword, String newPassword1,
                       String newPassword2) {
        checkValidPassword(newPassword1);
        if (!hasPassword(currentPassword)) {
            throw new UsersException(CAN_NOT_CHANGE_PASSWORD);
        }
        checkPasswordsMatch(newPassword2, newPassword1);
        this.password = newPassword1;
    }

    boolean hasPassword(String password) {
        return this.equals(new Password(password));
    }
}
