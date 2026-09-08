package Engine;

import java.io.Serializable;

class Trade implements Serializable {
    private final int id;
    private final int eventId;
    private final Option option;
    private final String buyerUsername;
    private final String sellerUsername;
    private final int quantity;
    private final double price;
    private final boolean minted;

    public Trade(int id, int eventId, Option option, String buyerUsername, String sellerUsername, int quantity, double price, boolean minted) {
        this.id = id;
        this.eventId = eventId;
        this.option = option;
        this.buyerUsername = buyerUsername;
        this.sellerUsername = sellerUsername;
        this.quantity = quantity;
        this.price = price;
        this.minted = minted;
    }

    public int id() {
        return id;
    }

    public int eventId() {
        return eventId;
    }

    public Option option() {
        return option;
    }

    public String buyerUsername() {
        return buyerUsername;
    }

    public String sellerUsername() {
        return sellerUsername;
    }

    public int quantity() {
        return quantity;
    }

    public double price() {
        return price;
    }

    public boolean minted() {
        return minted;
    }
}
