package model;

import api.UserProfile;
import api.UsersException;
import common.Email;
import common.NotBlankString;
import jakarta.persistence.*;
import lombok.*;

import java.util.Map;

import static model.Schema.*;

@Entity
@Table(name = USER_ENTITY_TABLE_NAME, schema = DATABASE_SCHEMA_NAME)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = {"userName"})
public class User {

    static final String INVALID_USERNAME = "A valid username must be provided";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = USER_ID_COLUMN_NAME)
    private long id;
    @Column(unique = true)
    private String userName;
    private String name;
    private String surname;
    private String email;
    // password must not escape by any means out of this object
    @Embedded
    private Password password;

    public User(String name, String surname, String email, String userName, String password,
                String repeatPassword) {
        this.password = new Password(password);
        this.password.checkPasswordsMatch(password, repeatPassword);
        this.name = name;
        this.surname = surname;
        this.email = new Email(email).asString();
        this.userName = new NotBlankString(userName,
                new UsersException(INVALID_USERNAME)).value();
    }

    boolean hasPassword(String password) {
        return this.password.hasPassword(password);
    }

    public void changePassword(String currentPassword, String newPassword1,
                               String newPassword2) {
        this.password.change(currentPassword, newPassword1, newPassword2);
    }

    String userName() {
        return userName;
    }

    public boolean hasName(String aName) {
        return this.name.equals(aName);
    }

    public boolean hasSurname(String aSurname) {
        return this.surname.equals(aSurname);
    }

    public boolean hasUsername(String aUserName) {
        return this.userName.equals(aUserName);
    }

    String email() {
        return this.email;
    }

    public Map<String, Object> toMap() {
        return Map.of("id", this.id);
    }

    Long id() {
        return id;
    }

    public UserProfile toProfile() {
        return new UserProfile(this.id, this.fullName(), this.userName,
                this.email);
    }

    private String fullName() {
        return this.name + " " + this.surname;
    }
}
