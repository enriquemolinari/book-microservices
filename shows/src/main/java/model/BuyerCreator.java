package model;

import common.Tx;
import jakarta.persistence.EntityManagerFactory;

public class BuyerCreator {
    private final EntityManagerFactory emf;

    public BuyerCreator(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void newUser(long userId) {
        new Tx(this.emf).inTx(em -> {
            em.persist(new Buyer(userId));
        });
    }

}
