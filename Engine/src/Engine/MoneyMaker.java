package Engine;

class MoneyMaker {
    private final int id;
    private double balance;

    MoneyMaker(int id, double balance) {
        this.id = id;
        this.balance = balance;
    }

    int id() {
        return id;
    }

    double balance() {
        return balance;
    }

    void adjustBalance(double delta) {
        this.balance += delta;
    }
}
