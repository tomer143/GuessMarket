package Engine.External;

import java.util.List;

public class EventDetails {
    private final int id;
    private final String name;
    private final String description;
    private final int feePercent;
    private final FeeCollection feeCollection;
    private final List<String> optionNames;
    private final boolean isActive;

    public EventDetails(int id, String name, String description, int feePercent, FeeCollection feeCollection, List<String> optionNames, boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.feePercent = feePercent;
        this.feeCollection = feeCollection;
        this.optionNames = optionNames;
        this.isActive = isActive;
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

    public boolean isActive() {
        return isActive;
    }
}
