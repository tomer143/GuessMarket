package Engine;

import java.io.Serializable;

class Holding implements Serializable {
    private final int eventId;
    private final int optionId;
    private final String username;
    private int quantity;
    private double amountPaid;

    public Holding(int eventId, int optionId, String username) {
        this.eventId = eventId;
        this.optionId = optionId;
        this.username = username;
        this.quantity = 0;
        this.amountPaid = 0;
    }

    public int eventId() {
        return eventId;
    }

    public int optionId() {
        return optionId;
    }

    public String username() {
        return username;
    }

    public int quantity() {
        return quantity;
    }

    public double amountPaid() {
        return amountPaid;
    }

    public void applyBuy(int qty, double cost) {
        this.quantity += qty;
        this.amountPaid += cost;
    }

    public void applySell(int qty, double proceeds) {
        if (this.quantity > 0)
            this.amountPaid -= this.amountPaid * ((double) qty / this.quantity);
        this.quantity -= qty;
    }
}
