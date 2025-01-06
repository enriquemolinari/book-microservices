package model;

import api.*;
import common.Tx;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class Movies implements MoviesSubSystem {
    static final String MOVIE_ID_DOES_NOT_EXISTS = "Movie ID not found";
    static final String USER_ID_NOT_EXISTS = "User not registered";
    static final String USER_HAS_ALREADY_RATE = "The user has already rate the movie";
    static final String PAGE_NUMBER_MUST_BE_GREATER_THAN_ZERO = "page number must be greater than zero";
    private static final int NUMBER_OF_RETRIES = 2;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private final EntityManagerFactory emf;
    private final int pageSize;

    public Movies(EntityManagerFactory emf, int pageSize) {
        this.emf = emf;
        this.pageSize = pageSize;
    }

    public Movies(EntityManagerFactory emf) {
        this(emf, DEFAULT_PAGE_SIZE);
    }

    @Override
    public DetailedMovieInfo movie(Long id) {
        return new Tx(this.emf).inTx(em -> {
            try {
                return movieWithActorsById(id, em);
            } catch (NonUniqueResultException | NoResultException e) {
                throw new MoviesException(MOVIE_ID_DOES_NOT_EXISTS);
            }
        });
    }

    public void modifyMovieName(Long id, String newName) {
        new Tx(this.emf).inTx(em -> {
            var movien = movieBy(id, em);
            movien.name(newName);
            return null;
        });
    }


    @Override
    public List<MovieInfo> allMovieInfosBy(List<Long> ids) {
        return new Tx(this.emf).inTx(em -> {
            return em.createQuery("from Movie m "
                            + "join fetch m.genres g "
                            + "where m.id IN ?1 "
                            + "order by m.name asc", Movie.class)
                    .setHint("org.hibernate.cacheable", "true")
                    .setParameter(1, ids).getResultList().stream()
                    .map(Movie::toInfo).toList();
        });
    }

    private DetailedMovieInfo movieWithActorsById(Long id, EntityManager em) {
        return em
                .createQuery("from Movie m "
                        + "join fetch m.actors a "
                        + "join fetch m.actors.person "
                        + "where m.id = ?1 "
                        + "order by m.name asc", Movie.class)
                .setParameter(1, id).getSingleResult().toDetailedInfo();
    }

    @Override
    public DetailedMovieInfo addNewMovie(String name, int duration,
                                         LocalDate releaseDate, String plot, Set<Genre> genres) {
        return new Tx(this.emf).inTx(em -> {
            var movie = new Movie(name, plot, duration, releaseDate, genres);
            em.persist(movie);
            return movie.toDetailedInfo();
        });
    }

    @Override
    public DetailedMovieInfo addActorTo(Long movieId, String name, String surname,
                                        String email, String characterName) {
        return new Tx(this.emf).inTx(em -> {
            var movie = em.getReference(Movie.class, movieId);
            movie.addAnActor(name, surname, email, characterName);
            return movie.toDetailedInfo();
        });
    }

    @Override
    public DetailedMovieInfo addDirectorToMovie(Long movieId, String name,
                                                String surname, String email) {
        return new Tx(this.emf).inTx(em -> {
            var movie = em.getReference(Movie.class, movieId);
            movie.addADirector(name, surname, email);
            return movie.toDetailedInfo();
        });
    }

    @Override
    public UserMovieRate rateMovieBy(Long userId, Long movieId, int rateValue,
                                     String comment) {
        return new Tx(emf).inTxWithRetriesOnConflict(em -> {
            checkUserIsRatingSameMovieTwice(userId, movieId, em);
            var user = userBy(userId, em);
            var movie = movieBy(movieId, em);

            var userRate = movie.rateBy(user, rateValue, comment);
            return userRate.toUserMovieRate();
        }, NUMBER_OF_RETRIES);
    }

    private void checkUserIsRatingSameMovieTwice(Long userId, Long movieId, EntityManager em) {
        var q = em.createQuery(
                "select ur from UserRate ur where ur.user.id = ?1 and movie.id = ?2",
                UserRate.class);
        q.setParameter(1, userId);
        q.setParameter(2, movieId);
        var mightHaveRated = q.getResultList();
        if (!mightHaveRated.isEmpty()) {
            throw new MoviesException(USER_HAS_ALREADY_RATE);
        }
    }

    private Movie movieBy(Long movieId, EntityManager em) {
        return findByIdOrThrows(Movie.class, movieId, MOVIE_ID_DOES_NOT_EXISTS, em);
    }

    private User userBy(Long userId, EntityManager em) {
        return findByIdOrThrows(User.class, userId, USER_ID_NOT_EXISTS, em);
    }

    <T> T findByIdOrThrows(Class<T> entity, Long id, String msg, EntityManager em) {
        var e = em.find(entity, id);
        if (e == null) {
            throw new MoviesException(msg);
        }
        return e;
    }

    @Override
    public List<UserMovieRate> pagedRatesOfOrderedDate(Long movieId,
                                                       int pageNumber) {
        checkPageNumberIsGreaterThanZero(pageNumber);
        return new Tx(emf).inTx(em -> {
            var q = em.createQuery(
                    "select ur from UserRate ur "
                            + "where ur.movie.id = ?1 "
                            + "order by ur.ratedAt desc",
                    UserRate.class);
            q.setParameter(1, movieId);
            q.setFirstResult((pageNumber - 1) * this.pageSize);
            q.setMaxResults(this.pageSize);
            return q.getResultList().stream()
                    .map(UserRate::toUserMovieRate).toList();
        });
    }

    @Override
    public List<DetailedMovieInfo> pagedSearchMovieByName(String fullOrPartmovieName,
                                                          int pageNumber) {
        checkPageNumberIsGreaterThanZero(pageNumber);
        return new Tx(emf).inTx(em -> {
            var q = em.createQuery(
                    "select m from Movie m "
                            // a trigram index is required
                            // on m.name to make this perform fine
                            + "where lower(m.name) like lower(?1) "
                            + "order by m.name desc",
                    Movie.class);
            q.setParameter(1, "%" + fullOrPartmovieName + "%");
            q.setFirstResult((pageNumber - 1) * this.pageSize);
            q.setMaxResults(this.pageSize);
            return q.getResultList().stream().map(Movie::toDetailedInfo).toList();
        });
    }

    private void checkPageNumberIsGreaterThanZero(int pageNumber) {
        if (pageNumber <= 0) {
            throw new MoviesException(PAGE_NUMBER_MUST_BE_GREATER_THAN_ZERO);
        }
    }

    @Override
    public List<DetailedMovieInfo> pagedMoviesSortedByName(int pageNumber) {
        checkPageNumberIsGreaterThanZero(pageNumber);
        return pagedMoviesSortedBy(pageNumber, "order by m.name");
    }

    @Override
    public List<DetailedMovieInfo> pagedMoviesSortedByReleaseDate(int pageNumber) {
        return pagedMoviesSortedBy(pageNumber, "order by m.releaseDate desc");
    }

    private List<DetailedMovieInfo> pagedMoviesSortedBy(int pageNumber,
                                                        String orderByClause) {
        checkPageNumberIsGreaterThanZero(pageNumber);
        return new Tx(this.emf).inTx(em -> {
            var q = em.createQuery(
                    "select m from Movie m "
                            + orderByClause,
                    Movie.class);
            q.setFirstResult((pageNumber - 1) * this.pageSize);
            q.setMaxResults(this.pageSize);
            return q.getResultList().stream().map(Movie::toDetailedInfo).toList();
        });
    }

    Long addNewUser(Long id, String username) {
        return new Tx(this.emf).inTx(em -> {
            em.persist(new User(id, username));
            return id;
        });
    }

    @Override
    public List<DetailedMovieInfo> pagedMoviesSortedByRate(int pageNumber) {
        return pagedMoviesSortedBy(pageNumber,
                "order by m.rating.totalUserVotes desc, m.rating.rateValue desc");
    }
}