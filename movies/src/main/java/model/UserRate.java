package model;

import api.MoviesException;
import api.UserMovieRate;
import common.FormattedDateTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static model.Schema.DATABASE_SCHEMA_NAME;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
@Table(schema = DATABASE_SCHEMA_NAME, uniqueConstraints = {
        @UniqueConstraint(name = "USER_CANT_RATE_A_MOVIE_MORE_THAN_ONCE", columnNames = {
                "movie_id", "user_id"})})
class UserRate {

    static final String INVALID_RATING = "Rate value must be an integer value between 0 and 5";
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    private int value;
    private String comment;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;
    private LocalDateTime ratedAt;

    public UserRate(User user, int value, String comment, Movie movie) {
        checkValidRateValue(value);
        this.user = user;
        this.value = value;
        this.comment = comment;
        this.movie = movie;
        this.ratedAt = LocalDateTime.now();
    }

    private void checkValidRateValue(int value) {
        if (value < 0 || value > 5) {
            throw new MoviesException(INVALID_RATING);
        }
    }

    public UserMovieRate toUserMovieRate() {
        return new UserMovieRate(this.user.id(), value,
                new FormattedDateTime(ratedAt).toString(), comment);
    }
}
