package model;

import api.MovieShows;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import static model.Schema.DATABASE_SCHEMA_NAME;
import static model.Schema.MOVIE_ID_COLUMN_NAME;

@Entity
@Table(schema = DATABASE_SCHEMA_NAME)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
public class Movie {
    @Id
    @Column(name = MOVIE_ID_COLUMN_NAME)
    private long id;
    @OneToMany(mappedBy = "movieToBeScreened")
    private List<ShowTime> showTimes;

    public Movie(long id) {
        this.id = id;
    }

    public MovieShows toMovieShow() {
        return new MovieShows(this.id,
                this.showTimes.stream()
                        .map(ShowTime::toShowInfo).toList());
    }

    public Long id() {
        return this.id;
    }
}
