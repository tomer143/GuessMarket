package Models.External;

public class BalanceLedgerEntry {
    private final String description;
    private final double amount;
    private final double resultingBalance;

    public BalanceLedgerEntry(String description, double amount, double resultingBalance) {
        this.description = description;
        this.amount = amount;
        this.resultingBalance = resultingBalance;
    }

    public String description() {
        return description;
    }

    public double amount() {
        return amount;
    }

    public double resultingBalance() {
        return resultingBalance;
    }
}
