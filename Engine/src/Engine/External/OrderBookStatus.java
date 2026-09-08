package Engine.External;

import java.util.List;

public class OrderBookStatus {
    private final int eventId;
    private final String eventName;
    private final EventPhase phase;
    private final int baseValue;
    private final List<OptionMarketData> options;
    private final double accountBalance;
    private final double totalFeeCollected;
    private final List<TradeHistoryRecord> history;
    private final String winningOptionName;

    public OrderBookStatus(int eventId, String eventName, EventPhase phase, int baseValue, List<OptionMarketData> options, double accountBalance, double totalFeeCollected, List<TradeHistoryRecord> history, String winningOptionName) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.phase = phase;
        this.baseValue = baseValue;
        this.options = options;
        this.accountBalance = accountBalance;
        this.totalFeeCollected = totalFeeCollected;
        this.history = history;
        this.winningOptionName = winningOptionName;
    }

    public int eventId() {
        return eventId;
    }

    public String eventName() {
        return eventName;
    }

    public EventPhase phase() {
        return phase;
    }

    public int baseValue() {
        return baseValue;
    }

    public List<OptionMarketData> options() {
        return options;
    }

    public double accountBalance() {
        return accountBalance;
    }

    public double totalFeeCollected() {
        return totalFeeCollected;
    }

    public List<TradeHistoryRecord> history() {
        return history;
    }

    public String winningOptionName() {
        return winningOptionName;
    }
}
