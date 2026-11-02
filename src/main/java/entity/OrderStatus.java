package entity;

public enum OrderStatus {
    PENDING,            // order has been submitted but not yet accepted. Order can still be edited.
    OPEN,               // order has been accepted, can only be filled or canceled by the user
    FILLED,
    CANCELED,
    REJECTED
}
