package Engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class User implements Serializable {
    private final String username;
    private double balance;
    private boolean blocked;
    private final List<Double> balanceHistory = new ArrayList<>();

    public User(String username, double balance) {
        this.username = username;
        this.balance = balance;
        this.blocked = false;
        this.balanceHistory.add(balance);
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

    public void adjustBalance(double delta) {
        this.balance += delta;
        if (this.balance < 0)
            this.blocked = true;
        this.balanceHistory.add(this.balance);
    }

    public List<Double> balanceHistory() {
        return Collections.unmodifiableList(balanceHistory);
    }
}
