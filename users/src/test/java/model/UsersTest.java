package model;

import api.AuthException;
import api.UsersException;
import jakarta.persistence.EntityManagerFactory;
import main.EmfBuilder;
import model.events.NewUserEvent;
import model.queue.JQueueTable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UsersTest {
    public static final String ENRIUSER_USERNAME = "emolinari";
    public static final String ENRIUSER_PWD = "1234567895555";
    public static final String ENRIUSER_EMAIL = "enrique.molinari@gmail.com";
    public static final String ENRIUSER_NAME = "Enrique";
    public static final String ENRIUSER_SURNAME = "Molinari";
    public static final String CONN_STR = "jdbc:derby:memory:users;create=true";
    public static final String DB_USER = "app";
    public static final String DB_PWD = "app";
    private static final String JOSEUSER_SURNAME = "aSurname";
    private static final String JOSEUSER_NAME = "Jose";
    private static final String JOSEUSER_PASS = "password12345679";
    private static final String JOSEUSER_EMAIL = "jose@bla.com";
    private static final String JOSEUSER_USERNAME = "joseuser";
    private static final Long NON_EXISTENT_ID = -2L;
    private static EntityManagerFactory emf;
    private final ForTests tests = new ForTests();

    @BeforeAll
    public static void setUp() {
        emf = new EmfBuilder(DB_USER, DB_PWD)
                .memory(CONN_STR)
                .withDropAndCreateDDL()
                .build();
    }

    @AfterEach
    public void tearDown() {
        emf.getSchemaManager().truncate();
    }

    @Test
    public void loginOk() {
        var users = getUsers();
        registerUserJose(users);
        var token = users.login(JOSEUSER_USERNAME, JOSEUSER_PASS);
        assertEquals("aToken", token);
    }

    private Users getUsers() {
        return new Users(emf, tests.doNothingToken());
    }

    @Test
    public void loginFail() {
        var users = getUsers();
        registerUserJose(users);
        var e = assertThrows(AuthException.class, () -> {
            users.login(JOSEUSER_USERNAME, "wrongPassword");
            fail("A user has logged in with a wrong password");
        });

        assertEquals(Users.USER_OR_PASSWORD_ERROR, e.getMessage());
    }

    @Test
    public void registerAUserNameTwice() {
        var users = getUsers();
        registerUserJose(users);

        var e = assertThrows(UsersException.class, () -> {
            registerUserJose(users);
            fail("I have registered the same userName twice");
        });

        assertEquals(Users.USER_NAME_ALREADY_EXISTS, e.getMessage());
    }

    @Test
    public void userChangePassword() {
        var users = getUsers();
        var userId = registerUserJose(users);
        users.changePassword(userId, JOSEUSER_PASS, "123412341234",
                "123412341234");
    }

    @Test
    public void userChangePasswordDoesNotMatch() {
        var cinema = getUsers();
        var userId = registerUserJose(cinema);
        var e = assertThrows(UsersException.class, () -> {
            cinema.changePassword(userId, JOSEUSER_PASS, "123412341234",
                    "123412341294");
        });
        assertEquals(Password.PASSWORDS_MUST_BE_EQUALS, e.getMessage());
    }

    @Test
    public void usersProfileByIds() {
        var users = getUsers();
        var userIdJose = registerUserJose(users);
        var userIdEnri = registerUserEnri(users);
        var userProfiles = users.allUsersProfileBy(List.of(userIdEnri, userIdJose));
        assertEquals(2, userProfiles.size());
        var joseProfile = userProfiles.stream().filter(u -> u.username().equals(JOSEUSER_USERNAME)).toList().getFirst();
        var enriProfile = userProfiles.stream().filter(u -> u.username().equals(ENRIUSER_USERNAME)).toList().getFirst();
        assertEquals(ENRIUSER_EMAIL, enriProfile.email());
        assertEquals(ENRIUSER_NAME + " " + ENRIUSER_SURNAME,
                enriProfile.fullname());
        assertEquals(JOSEUSER_EMAIL, joseProfile.email());
        assertEquals(JOSEUSER_NAME + " " + JOSEUSER_SURNAME,
                joseProfile.fullname());
    }

    @Test
    public void userProfileFrom() {
        var users = getUsers();
        var userId = registerUserJose(users);
        var profile = users.profileFrom(userId);
        assertEquals(JOSEUSER_USERNAME, profile.username());
        assertEquals(JOSEUSER_EMAIL, profile.email());
        assertEquals(JOSEUSER_NAME + " " + JOSEUSER_SURNAME,
                profile.fullname());
    }

    @Test
    public void userIdNotExists() {
        var cinema = getUsers();
        var e = assertThrows(UsersException.class, () -> {
            cinema.profileFrom(NON_EXISTENT_ID);
            fail("UserId should not exists in the database");
        });
        assertEquals(Users.USER_ID_NOT_EXISTS, e.getMessage());
    }

    @Test
    public void addRegisterNewUserPublishEvent() {
        var users = getUsers();
        var userId = users.registerUser(JOSEUSER_NAME, JOSEUSER_SURNAME,
                JOSEUSER_EMAIL,
                JOSEUSER_USERNAME,
                JOSEUSER_PASS, JOSEUSER_PASS);
        List<JQueueTable> jQueueTables = users.allQueued();
        assertEquals(1, jQueueTables.size());
        assertEquals(new NewUserEvent(userId).toJson(), jQueueTables.getFirst().getData());
    }

    private Long registerUserJose(Users users) {
        return users.registerUser(JOSEUSER_NAME, JOSEUSER_SURNAME,
                JOSEUSER_EMAIL,
                JOSEUSER_USERNAME,
                JOSEUSER_PASS, JOSEUSER_PASS);
    }

    private Long registerUserEnri(Users users) {
        return users.registerUser(ENRIUSER_NAME, ENRIUSER_SURNAME,
                ENRIUSER_EMAIL,
                ENRIUSER_USERNAME,
                ENRIUSER_PWD, ENRIUSER_PWD);
    }


}
