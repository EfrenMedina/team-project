package entity;

import java.util.UUID;

public class UuidIdGenerator implements IdGenerator{
    public String nextId(){
        return UUID.randomUUID().toString();
    }
}
