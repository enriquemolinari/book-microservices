package model.queue;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static model.Schema.DATABASE_SCHEMA_NAME;
import static model.queue.JQueueTable.JQUEUE_TABLE_NAME;

@Entity
@Table(name = JQUEUE_TABLE_NAME, schema = DATABASE_SCHEMA_NAME)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PUBLIC)
public class JQueueTable {
    static final String JQUEUE_TABLE_NAME = "jqueue";
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String channel;
    @Lob
    private String data;
    @Column(nullable = true)
    private Integer attempt;
    @Column(nullable = true)
    private Integer delay;
    @Column(name = "pushed_at", nullable = true)
    private LocalDateTime pushedAt;

    static String tableName() {
        return DATABASE_SCHEMA_NAME + "." + JQUEUE_TABLE_NAME;
    }
}
