package Engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class User implements Serializable {
    private final String username;
    private double balance;
    private boolean blocked;
    private final List<BalanceLedgerLine> ledger = new ArrayList<>();

    public User(String username, double balance) {
        this.username = username;
        this.balance = balance;
        this.blocked = false;
        this.ledger.add(new BalanceLedgerLine("Initial balance", balance, balance));
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

    public void adjustBalance(double delta, String description) {
        this.balance += delta;
        if (this.balance < 0)
            this.blocked = true;
        this.ledger.add(new BalanceLedgerLine(description, delta, this.balance));
    }

    public List<BalanceLedgerLine> ledger() {
        return Collections.unmodifiableList(ledger);
    }
}
