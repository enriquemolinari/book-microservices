package model;

import common.Tx;
import jakarta.persistence.EntityManagerFactory;

public class UserCreator {
    private final EntityManagerFactory emf;

    public UserCreator(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void newUser(long userId) {
        new Tx(this.emf).inTx(em -> {
            em.persist(new User(userId));
        });
    }
}
