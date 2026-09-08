package Engine;

import java.io.Serializable;

class User implements Serializable {
    private final String username;
    private double balance;
    private boolean blocked;

    public User(String username, double balance) {
        this.username = username;
        this.balance = balance;
        this.blocked = false;
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
    }
}
