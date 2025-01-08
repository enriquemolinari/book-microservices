package model;

import api.ShowsException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import common.NotBlankString;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class MovieInfoJsonToMap {

    public static final String REQUIRES_A_VALID_JSON_STRING = "Requires a valid json string";
    public static final String NO_MOVIES_FOUND_IN_JSON_STRING = "No movies found in json string";
    public static final String INVALID_JSON_STRING_FORMAT = "Invalid json string format";
    private final String json;

    public MovieInfoJsonToMap(String json) {
        this.json = new NotBlankString(json
                , new ShowsException(REQUIRES_A_VALID_JSON_STRING)).value();
    }

    public Map<Long, MovieInfo> convert() {
        try {
            List<MovieInfo> movies = new Gson().fromJson(json, new TypeToken<List<MovieInfo>>() {
            }.getType());
            checkAtLeastOneMovieWasSerialized(movies);
            return movies.stream()
                    .collect(Collectors.toMap(m -> m.id(),
                            movieInfo -> movieInfo));
        } catch (JsonSyntaxException e) {
            throw new ShowsException(INVALID_JSON_STRING_FORMAT, e);
        }
    }

    private void checkAtLeastOneMovieWasSerialized(List<MovieInfo> movies) {
        if (movies == null || movies.isEmpty()) {
            throw new ShowsException(NO_MOVIES_FOUND_IN_JSON_STRING);
        }
    }
}
