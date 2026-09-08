package Engine;

import Engine.External.OrderAction;

import java.io.Serializable;

class Order implements Serializable {
    private final int id;
    private final int eventId;
    private final int optionId;
    private final OrderAction side;
    private final String username;
    private final double price;
    private int remainingQuantity;

    public Order(int id, int eventId, int optionId, OrderAction side, String username, double price, int quantity) {
        this.id = id;
        this.eventId = eventId;
        this.optionId = optionId;
        this.side = side;
        this.username = username;
        this.price = price;
        this.remainingQuantity = quantity;
    }

    public int id() {
        return id;
    }

    public int eventId() {
        return eventId;
    }

    public int optionId() {
        return optionId;
    }

    public OrderAction side() {
        return side;
    }

    public String username() {
        return username;
    }

    public double price() {
        return price;
    }

    public int remainingQuantity() {
        return remainingQuantity;
    }

    public void fill(int quantity) {
        this.remainingQuantity -= quantity;
    }
}
