package model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class EntityCreator {
    private final EntityManagerFactory emf;

    public EntityCreator(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void persist(Object entity, long id) {
        emf.runInTransaction(em -> {
            createEntityIfNotExists(id, entity, em);
        });
    }

    private void createEntityIfNotExists(long id, Object entity, EntityManager em) {
        if (!exists(id, entity)) {
            em.persist(entity);
        }
    }

    public boolean exists(long userId, Object entity) {
        return emf.callInTransaction(em -> {
            var andEntity = em.find(entity.getClass(), userId);
            return andEntity != null;
        });
    }
}
