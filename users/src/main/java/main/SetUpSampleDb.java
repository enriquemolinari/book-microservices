package main;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import model.User;

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

            var eu = new User("Enrique",
                    "Molinari",
                    "enrique.molinari@gmail.com",
                    "emolinari",
                    "123456789012",
                    "123456789012");
            var nu = new User("Nicolas",
                    "Molimini",
                    "nico@mymovies.com",
                    "nico",
                    "123456789012",
                    "123456789012");
            var lu = new User("Lucia",
                    "Molimini",
                    "lu@mymovies.com",
                    "lucia",
                    "123456789012",
                    "123456789012");
            var ju = new User("Josefina",
                    "Simini",
                    "jsimini@mymovies.com",
                    "jsimini",
                    "123456789012",
                    "123456789012");

            //this order needs to be respected due to others modules users data
            //this is a bit fragile, needs to be improved
            em.persist(eu);
            em.persist(nu);
            em.persist(lu);
            em.persist(ju);

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

