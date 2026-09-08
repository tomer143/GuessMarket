package Engine;

import java.io.Serializable;

class Purchase implements Serializable {
    private final int eventId;
    private final int amount;
    private final double price;
    private final double feeAmount;
    private final Option option;
    private final String username;

    public Purchase(int eventId, int amount, double price, double feeAmount, Option option, String username) {
        this.eventId = eventId;
        this.amount = amount;
        this.price = price;
        this.feeAmount = feeAmount;
        this.option = option;
        this.username = username;
    }

    public int eventId() {
        return eventId;
    }

    public int amount() {
        return amount;
    }

    public double price() {
        return price;
    }

    public double feeAmount() {
        return feeAmount;
    }

    public Option option() {
        return option;
    }

    public String username() {
        return username;
    }
}
