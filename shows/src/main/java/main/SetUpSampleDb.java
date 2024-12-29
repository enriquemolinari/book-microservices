package main;

import common.DateTimeProvider;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import model.Buyer;
import model.Movie;
import model.ShowTime;
import model.Theater;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
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

            var schoolMovie = new Movie(1L, "Rock in the School", 109,
                    LocalDate.now(), Set.of("COMEDY", "ACTION"));

            var eu = new Buyer(1L);
            em.persist(eu);

            var nu = new Buyer(2L);
            var lu = new Buyer(3L);

            em.persist(nu);
            em.persist(lu);
            em.flush();

            em.persist(schoolMovie);
            em.flush();

            var fishMovie = new Movie(2L, "Small Fish", 125,
                    LocalDate.now().minusDays(1),
                    Set.of("ADVENTURE", "DRAMA"));

            em.persist(fishMovie);
            em.flush();

            var ju = new Buyer(4L);
            em.persist(ju);
            em.flush();

            var teaMovie = new Movie(3L, "Crash Tea", 105,
                    LocalDate.now().minusDays(3), Set.of("COMEDY"));
            em.persist(teaMovie);

            var runningMovie = new Movie(4L, "Running far Away",
                    105, LocalDate.now(), Set.of("THRILLER", "ACTION"));
            em.persist(runningMovie);

            // Seats from Theatre A
            Set<Integer> seatsA = new HashSet<>();
            for (int i = 1; i <= 30; i++) {
                seatsA.add(i);
            }

            var ta = new Theater("Theatre A", seatsA);

            em.persist(ta);
            em.flush();

            // Seats from Theatre B
            Set<Integer> seatsB = new HashSet<>();
            for (int i = 1; i <= 50; i++) {
                seatsB.add(i);
            }

            var tb = new Theater("Theatre B", seatsB);

            em.persist(tb);
            em.flush();

            var show1 = new ShowTime(DateTimeProvider.create(), fishMovie,
                    LocalDateTime.now().plusDays(1), 10f, ta);
            em.persist(show1);

            var show2 = new ShowTime(DateTimeProvider.create(), fishMovie,
                    LocalDateTime.now().plusDays(1).plusHours(4), 10f, ta);
            em.persist(show2);

            var show3 = new ShowTime(DateTimeProvider.create(), schoolMovie,
                    LocalDateTime.now().plusDays(2).plusHours(1), 19f, tb);

            em.persist(show3);

            var show4 = new ShowTime(DateTimeProvider.create(), schoolMovie,
                    LocalDateTime.now().plusDays(2).plusHours(5), 19f, tb);
            em.persist(show4);

            var show5 = new ShowTime(DateTimeProvider.create(), teaMovie,
                    LocalDateTime.now().plusDays(2).plusHours(2), 19f, ta);
            em.persist(show5);

            var show6 = new ShowTime(DateTimeProvider.create(), runningMovie,
                    LocalDateTime.now().plusHours(2), 19f, tb);
            em.persist(show6);

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

