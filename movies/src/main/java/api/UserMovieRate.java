package api;

public record UserMovieRate(Long userId, int rateValue, String ratedInDate,
                            String comment) {

}
