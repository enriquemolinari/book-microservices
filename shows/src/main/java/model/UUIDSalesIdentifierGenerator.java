package model;

import java.util.UUID;

public class UUIDSalesIdentifierGenerator implements SalesIdentifierGenerator {
    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
