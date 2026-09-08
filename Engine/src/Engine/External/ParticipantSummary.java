package Engine.External;

import java.util.List;

public class ParticipantSummary {
    private final String username;
    private final List<HoldingSummary> holdings;

    public ParticipantSummary(String username, List<HoldingSummary> holdings) {
        this.username = username;
        this.holdings = holdings;
    }

    public String username() {
        return username;
    }

    public List<HoldingSummary> holdings() {
        return holdings;
    }
}
