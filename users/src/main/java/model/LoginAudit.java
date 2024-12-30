package model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static model.Schema.DATABASE_SCHEMA_NAME;

@Entity
@Table(schema = DATABASE_SCHEMA_NAME)
@Getter(value = AccessLevel.PRIVATE)
@Setter(value = AccessLevel.PRIVATE)
@NoArgsConstructor
class LoginAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private LocalDateTime loginDateTime;
    @ManyToOne
    private User user;

    public LoginAudit(LocalDateTime loginDateTime, User user) {
        this.loginDateTime = loginDateTime;
        this.user = user;
    }
}
