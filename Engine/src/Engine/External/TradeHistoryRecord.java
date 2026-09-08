package Engine.External;

public class TradeHistoryRecord {
    private final String optionName;
    private final String buyerUsername;
    private final String sellerUsername;
    private final int quantity;
    private final double price;
    private final boolean minted;

    public TradeHistoryRecord(String optionName, String buyerUsername, String sellerUsername, int quantity, double price, boolean minted) {
        this.optionName = optionName;
        this.buyerUsername = buyerUsername;
        this.sellerUsername = sellerUsername;
        this.quantity = quantity;
        this.price = price;
        this.minted = minted;
    }

    public String optionName() {
        return optionName;
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
