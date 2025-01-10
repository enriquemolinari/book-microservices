package model;

import api.MovieShows;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

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
    
    public MovieShows toMovieShow(Map<Long, MovieInfo> movies) {
        return new MovieShows(this.id, movies.get(this.id).name(),
                movies.get(this.id).duration(),
                movies.get(this.id).genres(),
                this.showTimes.stream()
                        .map(ShowTime::toShowInfo).toList());
    }

    public Long id() {
        return this.id;
    }
}
