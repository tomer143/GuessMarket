package Engine;

class Purchase {
    private final int eventId;
    private final int amount;
    private final double price;
    private final Option option;

    Purchase(int eventId, int amount, double price, Option option) {
        this.eventId = eventId;
        this.amount = amount;
        this.price = price;
        this.option = option;
    }

    int eventId() {
        return eventId;
    }

    int amount() {
        return amount;
    }

    double price() {
        return price;
    }

    Option option() {
        return option;
    }
}
