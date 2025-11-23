package web;

public final class Routes {
    public static final String MOVIES = "/movies";
    public static final String MOVIES_SORTED_RATE = MOVIES + "/sorted/rate";
    public static final String MOVIES_SORTED_RELEASE_DATE = MOVIES + "/sorted/releasedate";
    public static final String MOVIES_LIST = MOVIES;
    static final String ID = "{id}";
    public static final String MOVIES_BY_ID = MOVIES + "/" + ID;
    public static final String MOVIES_RATES = MOVIES + "/" + ID + "/rate";
    static final String IDS = "{ids}";
    public static final String MOVIES_BY_IDS = MOVIES + "/by/" + IDS;
    static final String FULL_NAME = "{fullOrPartialName}";
    public static final String MOVIES_SEARCH = MOVIES + "/search/" + FULL_NAME;
    static final String MOVIE_ID = "{movieId}";
    private static final String PRIVATE = "private";
    public static final String MOVIES_PRIVATE = MOVIES + "/" + PRIVATE;
    public static final String MOVIES_PRIVATE_RATE = MOVIES_PRIVATE + "/" + MOVIE_ID + "/rate";
    public static final String MOVIES_PRIVATE_NEW = MOVIES_PRIVATE + "/new";

    private Routes() {
    }
}

