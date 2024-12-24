package model;

import common.NotBlankString;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import api.MoviesException;

import static model.Schema.DATABASE_SCHEMA_NAME;
import static model.Schema.USER_ENTITY_TABLE_NAME;

@Entity
@Table(name = USER_ENTITY_TABLE_NAME, schema = DATABASE_SCHEMA_NAME)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = {"userName"})
public class User {

    static final String INVALID_USERNAME = "A valid username must be provided";

    @Id
    private long id;
    @Column(unique = true)
    private String userName;

    public User(long id, String userName) {
        this.id = id;
        this.userName = new NotBlankString(userName,
                new MoviesException(INVALID_USERNAME)).value();
    }


    public String userName() {
        return userName;
    }

    public boolean hasUsername(String aUserName) {
        return this.userName.equals(aUserName);
    }

    Long id() {
        return id;
    }
}
