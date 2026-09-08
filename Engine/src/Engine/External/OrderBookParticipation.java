package Engine.External;

import java.util.List;

public class OrderBookParticipation {
    private final List<HoldingSummary> holdings;
    private final double totalFeePaid;
    private final Double profitLoss;

    public OrderBookParticipation(List<HoldingSummary> holdings, double totalFeePaid, Double profitLoss) {
        this.holdings = holdings;
        this.totalFeePaid = totalFeePaid;
        this.profitLoss = profitLoss;
    }

    public List<HoldingSummary> holdings() {
        return holdings;
    }

    public double totalFeePaid() {
        return totalFeePaid;
    }

    public Double profitLoss() {
        return profitLoss;
    }
}
