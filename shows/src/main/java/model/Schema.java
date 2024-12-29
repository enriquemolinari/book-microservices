package model;

//TODO: desmantelar...
public interface Schema {
    String DATABASE_SCHEMA_NAME = "shows";
    String USER_ENTITY_TABLE_NAME = "buyer";
    String MOVIE_ENTITY_TABLE_NAME = "movie";
    String MOVIE_GENRES_TABLE_NAME = "movie_genres";
    String MOVIE_ID_COLUMN_NAME = "id";
    String MOVIE_NAME_COLUMN_NAME = "name";
    String MOVIE_DURATION_COLUMN_NAME = "duration";
    String MOVIE_RELEASEDATE_COLUMN_NAME = "releasedate";
    String MOVIE_GENRE_ID_COLUMN_NAME = "movie_id";
    String MOVIE_GENRE_NAME_COLUMN_NAME = "genre";
}
