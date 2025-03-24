package web;

import api.Genre;

import java.time.LocalDate;
import java.util.Set;

public record NewMovieRequest(String name, int duration, LocalDate releaseDate,
                              String plot, Set<Genre> genres) {
}
