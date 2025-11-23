package web;

public final class Routes {
    public static final String SHOWS = "/shows";
    public static final String SHOWS_BUYER = SHOWS + "/buyer";
    static final String SHOW_ID = "{showId}";
    static final String ID = "{id}";
    public static final String SHOW_BY_ID = SHOWS + "/" + ID;
    static final String SALES_IDENTIFIER = "{salesIdentifier}";
    public static final String SHOWS_SALES_BY_IDENTIFIER = SHOWS + "/sales/" + SALES_IDENTIFIER;
    private static final String PRIVATE = "private";
    public static final String SHOWS_PRIVATE_RESERVE = SHOWS + "/" + PRIVATE + "/" + SHOW_ID + "/reserve";
    public static final String SHOWS_PRIVATE_PAY = SHOWS + "/" + PRIVATE + "/" + SHOW_ID + "/pay";
    private static final String MOVIE = "movie";
    public static final String SHOWS_BY_MOVIE_ID = SHOWS + "/" + MOVIE + "/" + ID;

    private Routes() {
    }
}
