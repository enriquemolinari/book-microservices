package common;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.RollbackException;

import java.util.function.Consumer;
import java.util.function.Function;

public class Tx {

    private EntityManagerFactory emf;

    public Tx(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public <T> T inTxWithRetriesOnConflict(
            Function<EntityManager, T> toExecute, int numberOfRetries) {
        int retries = 0;

        while (retries < numberOfRetries) {
            try {
                return inTx(toExecute);
                // There is no a great way in JPA to detect a constraint
                // violation. I use RollbackException and retries one more
                // time for specific use cases
            } catch (RollbackException e) {
                // jakarta.persistence.RollbackException
                retries++;
            }
        }
        throw new RuntimeException(
                "Trasaction could not be completed due to concurrency conflic");
    }

    public <T> T inTx(Function<EntityManager, T> toExecute) {
        return executeInTransaction(em -> toExecute.apply(em));
    }

    public void inTx(Consumer<EntityManager> toExecute) {
        executeInTransaction(em -> {
            toExecute.accept(em);
            // just to conform the compiler
            return null;
        });
    }

    private <T> T executeInTransaction(Function<EntityManager, T> toExecute) {
        var em = emf.createEntityManager();
        var tx = em.getTransaction();

        try {
            tx.begin();

            T t = toExecute.apply(em);
            tx.commit();

            return t;
        } catch (Exception e) {
            tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
