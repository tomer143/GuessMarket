package Engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class OrderBook implements Serializable {
    private final int optionId;
    private final List<Order> bids;
    private final List<Order> asks;
    private Double lastTradePrice;
    private int totalSupply;

    public OrderBook(int optionId) {
        this.optionId = optionId;
        this.bids = new ArrayList<>();
        this.asks = new ArrayList<>();
        this.lastTradePrice = null;
        this.totalSupply = 0;
    }

    public int optionId() {
        return optionId;
    }

    public List<Order> bids() {
        return bids;
    }

    public List<Order> asks() {
        return asks;
    }

    public Double lastTradePrice() {
        return lastTradePrice;
    }

    public int totalSupply() {
        return totalSupply;
    }

    public void addBid(Order order) {
        this.bids.add(order);
    }

    public void addAsk(Order order) {
        this.asks.add(order);
    }

    public void removeIfFilled(Order order) {
        if (order.remainingQuantity() <= 0) {
            this.bids.remove(order);
            this.asks.remove(order);
        }
    }

    public void recordTrade(double price) {
        this.lastTradePrice = price;
    }

    public void increaseSupply(int amount) {
        this.totalSupply += amount;
    }

    public List<Order> bidsBestFirst() {
        return bids.stream().sorted(Comparator.comparingDouble(Order::price).reversed()).toList();
    }

    public List<Order> asksBestFirst() {
        return asks.stream().sorted(Comparator.comparingDouble(Order::price)).toList();
    }

    public Double bestBid() {
        return bids.stream().mapToDouble(Order::price).max().stream().boxed().findFirst().orElse(null);
    }

    public Double bestAsk() {
        return asks.stream().mapToDouble(Order::price).min().stream().boxed().findFirst().orElse(null);
    }
}
