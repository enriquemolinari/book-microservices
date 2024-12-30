package model;

import java.util.Map;

public class ForTests {

    static final String SUPER_MOVIE_PLOT = "a super movie that shows the life of ...";
    static final String SUPER_MOVIE_NAME = "a super movie";
    static final String OTHER_SUPER_MOVIE_NAME = "another super movie";


    Token doNothingToken() {
        return new Token() {
            @Override
            public Long verifyAndGetUserIdFrom(String token) {
                return 0L;
            }

            @Override
            public String tokenFrom(Map<String, Object> payload) {
                return "aToken";
            }
        };
    }

    User createUserCharly() {
        return new User("Carlos", "Edgun", "cedgun@mysite.com",
                "cedgun", "afbcdefghigg", "afbcdefghigg");
    }

    User createUserJoseph() {
        return new User("Joseph", "Valdun", "jvaldun@wabla.com",
                "jvaldun", "tabcd1234igg", "tabcd1234igg");
    }

    User createUserNicolas() {
        return new User(
                "Nicolas", "Molinari", "nmolinari@yesmy.com",
                "nmolinari", "oneplayminebrawl", "oneplayminebrawl");
    }
}