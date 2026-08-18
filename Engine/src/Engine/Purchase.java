package Engine;

class Purchase {
    private final int eventId;
    private final int amount;
    private final double price;
    private final Option option;

    public Purchase(int eventId, int amount, double price, Option option) {
        this.eventId = eventId;
        this.amount = amount;
        this.price = price;
        this.option = option;
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

    public Option option() {
        return option;
    }
}
