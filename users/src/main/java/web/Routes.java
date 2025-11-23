package web;

public final class Routes {
    public static final String USERS = "/users";
    public static final String USERS_REGISTER = USERS + "/register";
    public static final String USERS_LOGIN = USERS + "/login";
    public static final String USERS_TOKEN = USERS + "/token";
    private static final String IDS = "{ids}";
    public static final String USERS_PROFILE_BY_IDS = USERS + "/profile/by/" + IDS;
    private static final String PRIVATE = "private";
    public static final String USERS_PRIVATE = USERS + "/" + PRIVATE;
    public static final String USERS_PRIVATE_PROFILE = USERS_PRIVATE + "/profile";
    public static final String USERS_PRIVATE_CHANGEPASSWORD = USERS_PRIVATE + "/changepassword";
    public static final String USERS_PRIVATE_LOGOUT = USERS_PRIVATE + "/logout";

    private Routes() {
    }
}
