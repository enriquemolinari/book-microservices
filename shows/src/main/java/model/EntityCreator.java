package model;

import jakarta.persistence.EntityManagerFactory;

public class EntityCreator {
    private final EntityManagerFactory emf;

    public EntityCreator(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void persist(Object entity) {
        emf.runInTransaction(em -> {
            em.persist(entity);
        });
    }
}
