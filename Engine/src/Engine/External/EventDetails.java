package Engine.External;

import java.util.List;

public class EventDetails {
    private final int id;
    private final String name;
    private final String description;
    private final int feePercent;
    private final FeeCollection feeCollection;
    private final List<String> optionNames;
    private final EventPhase phase;
    private final TradingMethod method;
    private final String mmUsername;
    private final double accountBalance;

    public EventDetails(int id, String name, String description, int feePercent, FeeCollection feeCollection, List<String> optionNames, EventPhase phase, TradingMethod method, String mmUsername, double accountBalance) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.feePercent = feePercent;
        this.feeCollection = feeCollection;
        this.optionNames = optionNames;
        this.phase = phase;
        this.method = method;
        this.mmUsername = mmUsername;
        this.accountBalance = accountBalance;
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public int feePercent() {
        return feePercent;
    }

    public FeeCollection feeCollection() {
        return feeCollection;
    }

    public List<String> optionNames() {
        return optionNames;
    }

    public EventPhase phase() {
        return phase;
    }

    public TradingMethod method() {
        return method;
    }

    public String mmUsername() {
        return mmUsername;
    }

    public double accountBalance() {
        return accountBalance;
    }
}
