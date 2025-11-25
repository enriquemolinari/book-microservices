package model;

import jakarta.persistence.EntityManagerFactory;

public class UserCreator {
    private final EntityManagerFactory emf;

    public UserCreator(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void newUser(long userId) {
        emf.runInTransaction(em -> {
            em.persist(new User(userId));
        });
    }
}
