package Engine.External;

public class BalanceHistoryPoint {
    private final int step;
    private final double balance;

    public BalanceHistoryPoint(int step, double balance) {
        this.step = step;
        this.balance = balance;
    }

    public int step() {
        return step;
    }

    public double balance() {
        return balance;
    }
}
