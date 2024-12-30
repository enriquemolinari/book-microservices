package model;

import api.UserProfile;
import api.UsersException;
import common.Email;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    public void userCanBeCreated() {
        var u = createUserEnrique();

        assertTrue(u.hasPassword("Ab138RtoUjkL"));
        assertTrue(u.hasName("Enrique"));
        assertTrue(u.hasSurname("Molinari"));
        assertTrue(u.hasUsername("enriquemolinari"));
    }

    private User createUserEnrique() {
        return new User("Enrique", "Molinari", "enrique.molinari@gmail.com",
                "enriquemolinari", "Ab138RtoUjkL", "Ab138RtoUjkL");
    }

    @Test
    public void userNameIsInvalidWithNull() {
        Exception e = assertThrows(UsersException.class, () -> {
            new User(
                    "Enrique", "Molinari",
                    "enrique.molinari@gmail.com",
                    null, "Ab138RtoUjkL", "Ab138RtoUjkL");
        });
        assertEquals(User.INVALID_USERNAME, e.getMessage());
    }

    @Test
    public void userNameIsInvalid() {
        Exception e = assertThrows(UsersException.class, () -> {
            new User(
                    "Enrique", "Molinari",
                    "enrique.molinari@gmail.com",
                    "", "Ab138RtoUjkL", "Ab138RtoUjkL");
        });
        assertEquals(User.INVALID_USERNAME, e.getMessage());
    }

    @Test
    public void userEmailIsInvalid() {
        Exception e = assertThrows(RuntimeException.class, () -> {
            new User(
                    "Enrique", "Molinari",
                    "enrique.molinarigmail.com",
                    "emolinari", "Ab138RtoUjkL", "Ab138RtoUjkL");
        });
        assertEquals(Email.NOT_VALID_EMAIL, e.getMessage());
    }

    @Test
    public void userPasswordsDoesNotMatch() {
        Exception e = assertThrows(UsersException.class, () -> {
            new User(
                    "Enrique", "Molinari",
                    "enrique.molinari@gmail.com",
                    "emolinari", "Ab138RtoUjkL", "Ab13RtoUjkL");
        });
        assertEquals(Password.PASSWORDS_MUST_BE_EQUALS, e.getMessage());
    }

    @Test
    public void userPasswordIsInvalid() {
        Exception e = assertThrows(UsersException.class, () -> {
            new User(
                    "Enrique", "Molinari",
                    "enrique.molinari@gmail.com",
                    "emolinari", "abcAdif", "abcAdif");
        });
        assertEquals(Password.NOT_VALID_PASSWORD, e.getMessage());
    }

    @Test
    public void changePasswordCurrentPasswordNotTheSame() {
        var u = createUserEnrique();

        Exception e = assertThrows(UsersException.class, () -> {
            u.changePassword("abchd1239876", "Abcdefghijkl", "Abcdefghijkl");
        });
        assertEquals(Password.CAN_NOT_CHANGE_PASSWORD, e.getMessage());
    }

    @Test
    public void changePasswordNewPassword1And2DoesNotMatch() {
        var u = createUserEnrique();

        Exception e = assertThrows(UsersException.class, () -> {
            u.changePassword("Ab138RtoUjkL", "Abcdefghrjkl", "Abcdefghijkl");
        });
        assertEquals(Password.PASSWORDS_MUST_BE_EQUALS, e.getMessage());
    }

    @Test
    public void changePasswordNewPasswordNotValid() {
        var u = createUserEnrique();

        Exception e = assertThrows(UsersException.class, () -> {
            u.changePassword("Ab138RtoUjkL", "Abcdefgh", "Abcdefgh");
        });
        assertEquals(Password.NOT_VALID_PASSWORD, e.getMessage());
    }

    @Test
    public void changePasswordOk() {
        var u = createUserEnrique();

        u.changePassword("Ab138RtoUjkL", "Abcdefghijkl", "Abcdefghijkl");

        assertTrue(u.hasPassword("Abcdefghijkl"));
    }

    @Test
    public void newCreatedUserHasZeroPoints() {
        var u = createUserEnrique();
        assertTrue(u.hasPoints(0));
    }

    @Test
    public void userProfile() {
        var u = createUserEnrique();

        assertEquals(new UserProfile("Enrique Molinari", "enriquemolinari",
                "enrique.molinari@gmail.com", 0), u.toProfile());
    }

    @Test
    public void userEarnsSomePoints() {
        var u = createUserEnrique();
        u.newEarnedPoints(10);
        assertTrue(u.hasPoints(10));
    }

    @Test
    public void userEarnsAnInvalidNumberOfPoints() {
        var u = createUserEnrique();

        Exception e = assertThrows(UsersException.class, () -> {
            u.newEarnedPoints(0);
        });
        assertEquals(User.POINTS_MUST_BE_GREATER_THAN_ZERO, e.getMessage());
    }

}
