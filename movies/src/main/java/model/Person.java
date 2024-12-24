package model;

import common.Email;
import common.NotBlankString;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import api.MoviesException;

import static model.Schema.DATABASE_SCHEMA_NAME;

@Entity
@Table(schema = DATABASE_SCHEMA_NAME)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
public class Person {

    static final String NAME_MUST_NOT_BE_BLANK = "Name must not be blank";
    static final String SURNAME_MUST_NOT_BE_BLANK = "Surname must not be blank";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;
    private String surname;
    private String email;

    public Person(String name, String surname, String email) {
        this.name = new NotBlankString(name, new MoviesException(NAME_MUST_NOT_BE_BLANK)).value();
        this.surname = new NotBlankString(surname, new MoviesException(SURNAME_MUST_NOT_BE_BLANK))
                .value();
        this.email = new Email(email).asString();
    }

    public boolean isNamed(String aName) {
        return this.fullName().equals(aName);
    }

    String fullName() {
        return this.name + " " + this.surname;
    }

    public boolean hasName(String aName) {
        return this.name.equals(aName);
    }

    public boolean aSurname(String aSurname) {
        return this.surname.equals(aSurname);
    }

    String email() {
        return this.email;
    }
}
