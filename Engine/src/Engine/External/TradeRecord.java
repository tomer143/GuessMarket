package Engine.External;

public class TradeRecord {
    private final String optionName;
    private final int amount;
    private final double pricePaid;

    public TradeRecord(String optionName, int amount, double pricePaid) {
        this.optionName = optionName;
        this.amount = amount;
        this.pricePaid = pricePaid;
    }

    public String optionName() {
        return optionName;
    }

    public int amount() {
        return amount;
    }

    public double pricePaid() {
        return pricePaid;
    }
}
