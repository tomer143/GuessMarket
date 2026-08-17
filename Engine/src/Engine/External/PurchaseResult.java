package Engine.External;

public class PurchaseResult {
    private final double sharesCost;
    private final double feeAmount;
    private final double totalPaid;

    public PurchaseResult(double sharesCost, double feeAmount, double totalPaid) {
        this.sharesCost = sharesCost;
        this.feeAmount = feeAmount;
        this.totalPaid = totalPaid;
    }

    public double sharesCost() {
        return sharesCost;
    }

    public double feeAmount() {
        return feeAmount;
    }

    public double totalPaid() {
        return totalPaid;
    }
}
