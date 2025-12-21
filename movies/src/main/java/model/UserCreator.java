package model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class UserCreator {
    private final EntityManagerFactory emf;

    public UserCreator(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void newUser(long userId) {
        emf.runInTransaction(em -> {
            createUserIfNotExists(userId, em);
        });
    }

    private void createUserIfNotExists(long userId, EntityManager em) {
        if (!exists(userId)) {
            em.persist(new User(userId));
        }
    }

    public boolean exists(long userId) {
        return emf.callInTransaction(em -> {
            var user = em.find(User.class, userId);
            return user != null;
        });
    }
}
