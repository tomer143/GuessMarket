package Engine.External;

public class HoldingSummary {
    private final String optionName;
    private final int quantity;
    private final double amountPaid;

    public HoldingSummary(String optionName, int quantity, double amountPaid) {
        this.optionName = optionName;
        this.quantity = quantity;
        this.amountPaid = amountPaid;
    }

    public String optionName() {
        return optionName;
    }

    public int quantity() {
        return quantity;
    }

    public double amountPaid() {
        return amountPaid;
    }
}
