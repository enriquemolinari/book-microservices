package main;

import api.Genre;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import model.Actor;
import model.Movie;
import model.Person;
import model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class SetUpSampleDb {

    private final EntityManagerFactory emf;

    public SetUpSampleDb(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void createSchemaAndPopulateSampleData() {
        var em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            var jake = new Person("Jake", "White", "jake@mymovies.com");
            var josh = new Person("Josh", "Blue", "josh@mymovies.com");
            var nervan = new Person("Nervan", "Allister",
                    "nervan@mymovies.com");
            var ernest = new Person("Ernest", "Finey", "ernest@mymovies.com");
            var enrique = new Person("Enrique", "Molinari",
                    "enrique.molinari@gmail.com");
            var josefina = new Person("Josefina", "Simini",
                    "jsimini@mymovies.com");
            var lucia = new Person("Lucia", "Molimini", "lu@mymovies.com");
            var nico = new Person("Nicolas", "Molimini", "nico@mymovies.com");
            var camilo = new Person("Camilo", "Fernandez", "cami@mymovies.com");
            var franco = new Person("Franco", "Elchow", "franco@mymovies.com");
            var michael = new Person("Michael", "Martinez",
                    "michael@mymovies.com");
            var michell = new Person("Michell", "Orenson",
                    "michell@mymovies.com");
            var craigDirector = new Person("Christopher", "Wegemen",
                    "craig@mymovies.com");
            var judithDirector = new Person("Jude", "Zevele",
                    "judith@mymovies.com");
            var andreDirector = new Person("Andres", "Lembert",
                    "andre@mymovies.com");
            var colinDirector = new Person("Colin", "Clefferd",
                    "andre@mymovies.com");

            var jakeActor = new Actor(jake, "Daniel Finne");
            var jakeActor2 = new Actor(jake, "Camilo Fernis");
            var joshActor = new Actor(josh, "Norber Carl");
            var ernestActor = new Actor(ernest, "Edward Blomsky (senior)");
            var nervanActor = new Actor(nervan, "Edward Blomsky (young)");
            var camiloActor = new Actor(camilo, "Judy");
            var francoActor = new Actor(franco, "George");
            var michaelActor = new Actor(michael, "Mike");
            var michellActor = new Actor(michell, "Teressa");

            var schoolMovie = new Movie("Rock in the School",
                    "A teacher tries to teach Rock & Roll music and history "
                            + "to elementary school kids",
                    109, LocalDate.now(), Set.of(Genre.COMEDY, Genre.ACTION),
                    List.of(jakeActor, joshActor), List.of(colinDirector));
            var eu = new User(1L, "emolinari");
            em.persist(eu);

            var nu = new User(2L, "nico");
            var lu = new User(3L, "lucia");

            em.persist(nu);
            em.persist(lu);

            schoolMovie.rateBy(eu, 5, "Great Movie");
            schoolMovie.rateBy(nu, 5,
                    "Fantastic! The actors, the music, everything is fantastic!");
            schoolMovie.rateBy(lu, 4, "I really enjoy the movie");

            em.persist(schoolMovie);

            var fishMovie = new Movie("Small Fish",
                    "A caring father teaches life values while fishing.", 125,
                    LocalDate.now().minusDays(1),
                    Set.of(Genre.ADVENTURE, Genre.DRAMA),
                    List.of(jakeActor2, ernestActor, nervanActor),
                    List.of(andreDirector));

            fishMovie.rateBy(eu, 4, "Fantastic !!");

            em.persist(fishMovie);

            var ju = new User(4L, "jsimini");
            em.persist(ju);

            var teaMovie = new Movie("Crash Tea", "A documentary about tea.",
                    105, LocalDate.now().minusDays(3), Set.of(Genre.COMEDY),
                    List.of(michaelActor, michellActor),
                    List.of(judithDirector, craigDirector));
            em.persist(teaMovie);

            var runningMovie = new Movie("Running far Away",
                    "José a sad person run away from his town looking for new adventures.",
                    105, LocalDate.now(), Set.of(Genre.THRILLER, Genre.ACTION),
                    List.of(francoActor, camiloActor), List.of(judithDirector));
            em.persist(runningMovie);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }
}
