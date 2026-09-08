package Engine.External;

public class FillRecord {
    private final int quantity;
    private final double price;
    private final double feeAmount;
    private final boolean minted;
    private final String counterpartyUsername;

    public FillRecord(int quantity, double price, double feeAmount, boolean minted, String counterpartyUsername) {
        this.quantity = quantity;
        this.price = price;
        this.feeAmount = feeAmount;
        this.minted = minted;
        this.counterpartyUsername = counterpartyUsername;
    }

    public int quantity() {
        return quantity;
    }

    public double price() {
        return price;
    }

    public double feeAmount() {
        return feeAmount;
    }

    public boolean minted() {
        return minted;
    }

    public String counterpartyUsername() {
        return counterpartyUsername;
    }
}
