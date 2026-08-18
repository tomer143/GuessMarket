package Engine;

import java.io.Serializable;

class MoneyMaker implements Serializable {
    private final int id;
    private double balance;

    public MoneyMaker(int id, double balance) {
        this.id = id;
        this.balance = balance;
    }

    public int id() {
        return id;
    }

    public double balance() {
        return balance;
    }

    public void adjustBalance(double delta) {
        this.balance += delta;
    }
}
