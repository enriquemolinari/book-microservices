package model;

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
public class Actor {

    static final String NAME_INVALID = "name must not be blank";
    static final String CHARACTER_NAME_INVALID = "character name must not be blank";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "id_person")
    private Person person;
    private String characterName;

    public Actor(Person person, String characterName) {
        this.person = person;
        this.characterName = new NotBlankString(characterName,
                new MoviesException(CHARACTER_NAME_INVALID)).value();
    }

    public boolean isNamed(String aName) {
        return this.person.isNamed(aName);
    }

    public boolean hasCharacterName(String aCharacterName) {
        return this.characterName.equals(aCharacterName);
    }

    String fullName() {
        return this.person.fullName();
    }

    String characterName() {
        return this.characterName;
    }
}
