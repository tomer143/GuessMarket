package Engine.External;

public class RestingOrderView {
    private final String username;
    private final int quantity;
    private final double price;

    public RestingOrderView(String username, int quantity, double price) {
        this.username = username;
        this.quantity = quantity;
        this.price = price;
    }

    public String username() {
        return username;
    }

    public int quantity() {
        return quantity;
    }

    public double price() {
        return price;
    }
}
