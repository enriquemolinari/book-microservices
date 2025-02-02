package model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static model.Schema.DATABASE_SCHEMA_NAME;
import static model.Schema.USER_ENTITY_TABLE_NAME;

@Entity
@Table(name = USER_ENTITY_TABLE_NAME, schema = DATABASE_SCHEMA_NAME)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
public class Buyer {
    @Id
    private long id;
    @OneToMany(cascade = CascadeType.PERSIST, mappedBy = "purchaser")
    private List<Sale> purchases;
    private long points;

    public Buyer(long id) {
        this.id = id;
        this.points = 0L;
        this.purchases = new ArrayList<>();
    }

    void newPurchase(Sale sale) {
        this.purchases.add(sale);
        this.points += sale.points();
    }

    Long id() {
        return id;
    }

    long points() {
        return points;
    }
}
