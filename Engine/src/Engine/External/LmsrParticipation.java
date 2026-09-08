package Engine.External;

import java.util.List;

public class LmsrParticipation {
    private final List<TradeRecord> history;
    private final double totalFeePaid;
    private final List<OptionStatus> optionStatuses;
    private final EventPhase phase;
    private final String winningOptionName;

    public LmsrParticipation(List<TradeRecord> history, double totalFeePaid, List<OptionStatus> optionStatuses, EventPhase phase, String winningOptionName) {
        this.history = history;
        this.totalFeePaid = totalFeePaid;
        this.optionStatuses = optionStatuses;
        this.phase = phase;
        this.winningOptionName = winningOptionName;
    }

    public List<TradeRecord> history() {
        return history;
    }

    public double totalFeePaid() {
        return totalFeePaid;
    }

    public List<OptionStatus> optionStatuses() {
        return optionStatuses;
    }

    public EventPhase phase() {
        return phase;
    }

    public String winningOptionName() {
        return winningOptionName;
    }
}
