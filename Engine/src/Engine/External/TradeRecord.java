package Engine.External;

public class TradeRecord {
    private final String optionName;
    private final int amount;
    private final double pricePaid;
    private final String otherOptionName;
    private final double otherOptionChance;

    public TradeRecord(String optionName, int amount, double pricePaid, String otherOptionName, double otherOptionChance) {
        this.optionName = optionName;
        this.amount = amount;
        this.pricePaid = pricePaid;
        this.otherOptionName = otherOptionName;
        this.otherOptionChance = otherOptionChance;
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

    public String otherOptionName() {
        return otherOptionName;
    }

    public double otherOptionChance() {
        return otherOptionChance;
    }
}
