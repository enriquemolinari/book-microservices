package model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static model.Schema.DATABASE_SCHEMA_NAME;
import static model.Schema.USER_ENTITY_TABLE_NAME;

@Entity
@Table(name = USER_ENTITY_TABLE_NAME, schema = DATABASE_SCHEMA_NAME)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
public class User {

    @Id
    private long id;

    public User(long id) {
        this.id = id;
    }

    Long id() {
        return id;
    }
}
