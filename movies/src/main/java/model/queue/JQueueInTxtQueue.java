package model.queue;

import ar.cpfw.jqueue.push.JTxQueue;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;

import java.sql.Connection;

public class JQueueInTxtQueue {

    private final EntityManager em;

    public JQueueInTxtQueue(EntityManager em) {
        this.em = em;
    }

    public void push(String data) {
        Session session = em.unwrap(Session.class);
        session.doWork(new Work() {
            @Override
            public void execute(Connection connection) {
                JTxQueue.queue(connection, JQueueTable.tableName())
                        .push(data);
            }
        });
    }
}
