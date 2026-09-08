package Engine.External;

public class UserEventParticipation {
    private final int eventId;
    private final String eventName;
    private final TradingMethod method;
    private final boolean isMm;

    public UserEventParticipation(int eventId, String eventName, TradingMethod method, boolean isMm) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.method = method;
        this.isMm = isMm;
    }

    public int eventId() {
        return eventId;
    }

    public String eventName() {
        return eventName;
    }

    public TradingMethod method() {
        return method;
    }

    public boolean isMm() {
        return isMm;
    }
}
