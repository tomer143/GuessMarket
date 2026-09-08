package Engine.External;

import java.util.List;

public class UserDetails {
    private final String username;
    private final double balance;
    private final boolean blocked;
    private final List<UserEventParticipation> participations;

    public UserDetails(String username, double balance, boolean blocked, List<UserEventParticipation> participations) {
        this.username = username;
        this.balance = balance;
        this.blocked = blocked;
        this.participations = participations;
    }

    public String username() {
        return username;
    }

    public double balance() {
        return balance;
    }

    public boolean blocked() {
        return blocked;
    }

    public List<UserEventParticipation> participations() {
        return participations;
    }
}
