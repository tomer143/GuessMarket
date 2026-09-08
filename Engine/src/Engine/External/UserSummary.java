package Engine.External;

public class UserSummary {
    private final String username;
    private final double balance;
    private final boolean blocked;

    public UserSummary(String username, double balance, boolean blocked) {
        this.username = username;
        this.balance = balance;
        this.blocked = blocked;
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
}
