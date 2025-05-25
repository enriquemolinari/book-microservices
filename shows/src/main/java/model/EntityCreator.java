package model;

import common.Tx;
import jakarta.persistence.EntityManagerFactory;

public class EntityCreator {
    private final EntityManagerFactory emf;

    public EntityCreator(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void persist(Object entity) {
        new Tx(this.emf).inTx(em -> {
            em.persist(entity);
        });
    }
}
