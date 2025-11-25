package common;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.RollbackException;

import java.util.function.Function;

public class Tx {

    private final EntityManagerFactory emf;

    public Tx(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public <T> T inTxWithRetriesOnConflict(
            Function<EntityManager, T> toExecute, int numberOfRetries) {
        int retries = 0;

        while (retries < numberOfRetries) {
            try {
                return emf.callInTransaction(toExecute);
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
}
