package Models.External;

import java.util.List;

public class EventStatus {
    private final int eventId;
    private final String eventName;
    private final EventPhase phase;
    private final List<OptionStatus> optionStatuses;
    private final double accountBalance;
    private final double totalFeeCollected;
    private final List<TradeRecord> history;
    private final String winningOptionName;

    public EventStatus(int eventId, String eventName, EventPhase phase, List<OptionStatus> optionStatuses, double accountBalance, double totalFeeCollected, List<TradeRecord> history, String winningOptionName) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.phase = phase;
        this.optionStatuses = optionStatuses;
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

    public List<OptionStatus> optionStatuses() {
        return optionStatuses;
    }

    public double accountBalance() {
        return accountBalance;
    }

    public double totalFeeCollected() {
        return totalFeeCollected;
    }

    public List<TradeRecord> history() {
        return history;
    }

    public String winningOptionName() {
        return winningOptionName;
    }
}
