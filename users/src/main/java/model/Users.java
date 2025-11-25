package model;

import api.AuthException;
import api.UserProfile;
import api.UsersException;
import api.UsersSubSystem;
import common.DateTimeProvider;
import common.Tx;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import model.events.NewUserEvent;
import model.queue.JQueueInTxtQueue;
import model.queue.JQueueTable;

import java.util.List;

public class Users implements UsersSubSystem {
    static final String USER_NAME_ALREADY_EXISTS = "userName already exists";
    static final String USER_ID_NOT_EXISTS = "User not registered";
    static final String USER_OR_PASSWORD_ERROR = "Invalid username or password";
    private static final int NUMBER_OF_RETRIES = 2;
    private final EntityManagerFactory emf;
    private final Token token;
    private final DateTimeProvider dateTimeProvider;

    public Users(EntityManagerFactory emf,
                 Token token, DateTimeProvider provider) {
        this.emf = emf;
        this.token = token;
        this.dateTimeProvider = provider;
    }

    public Users(EntityManagerFactory emf,
                 Token token) {
        this(emf, token, DateTimeProvider.create());
    }

    @Override
    public String login(String username, String password) {
        return emf.callInTransaction(em -> {
            var q = em.createQuery(
                    "select u from User u where u.userName = ?1 and u.password.password = ?2",
                    User.class);
            q.setParameter(1, username);
            q.setParameter(2, password);
            var mightBeAUser = q.getResultList();
            if (mightBeAUser.isEmpty()) {
                throw new AuthException(USER_OR_PASSWORD_ERROR);
            }
            var user = mightBeAUser.getFirst();
            em.persist(new LoginAudit(this.dateTimeProvider.now(), user));
            return token.tokenFrom(user.toMap());
        });
    }

    @Override
    public Long registerUser(String name, String surname, String email,
                             String userName,
                             String password, String repeatPassword) {
        return new Tx(emf).inTxWithRetriesOnConflict((em) -> {
            checkUserNameAlreadyExists(userName, em);
            var user = new User(name, surname, email, userName,
                    password,
                    repeatPassword);
            em.persist(user);
            new JQueueInTxtQueue(em).push(new NewUserEvent(user.id()).toJson());
            return user.id();
        }, NUMBER_OF_RETRIES);
    }

    private void checkUserNameAlreadyExists(String userName, EntityManager em) {
        var q = em.createQuery(
                "select u from User u where u.userName = ?1 ", User.class);
        q.setParameter(1, userName);
        var mightBeAUser = q.getResultList();
        if (!mightBeAUser.isEmpty()) {
            throw new UsersException(USER_NAME_ALREADY_EXISTS);
        }
    }

    @Override
    public Long userIdFrom(String token) {
        return this.token.verifyAndGetUserIdFrom(token);
    }

    @Override
    public UserProfile profileFrom(Long userId) {
        return emf.callInTransaction(em -> {
            return userBy(userId, em).toProfile();
        });
    }

    @Override
    public List<UserProfile> allUsersProfileBy(List<Long> ids) {
        return emf.callInTransaction(em -> {
            return em.createQuery("from User u "
                            + "where u.id IN ?1 ", User.class)
                    .setHint("org.hibernate.cacheable", "true")
                    .setParameter(1, ids).getResultList().stream()
                    .map(User::toProfile).toList();
        });
    }


    @Override
    public void changePassword(Long userId, String currentPassword,
                               String newPassword1, String newPassword2) {
        emf.runInTransaction(em -> {
            userBy(userId, em).changePassword(currentPassword, newPassword1,
                    newPassword2);
        });
    }

    User userBy(Long userId, EntityManager em) {
        return findByIdOrThrows(User.class, userId, Users.USER_ID_NOT_EXISTS, em);
    }

    <T> T findByIdOrThrows(Class<T> entity, Long id, String msg, EntityManager em) {
        var e = em.find(entity, id);
        if (e == null) {
            throw new UsersException(msg);
        }
        return e;
    }

    List<JQueueTable> allQueued() {
        return emf.callInTransaction(em -> {
            return em.createQuery("from JQueueTable", JQueueTable.class).getResultList();
        });
    }
}