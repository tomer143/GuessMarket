package Engine.External;

import java.util.List;

public class OptionMarketData {
    private final String optionName;
    private final Double lastTradePrice;
    private final Double bestBid;
    private final Double bestAsk;
    private final Double mid;
    private final Double spread;
    private final List<RestingOrderView> bids;
    private final List<RestingOrderView> asks;

    public OptionMarketData(String optionName, Double lastTradePrice, Double bestBid, Double bestAsk, Double mid, Double spread, List<RestingOrderView> bids, List<RestingOrderView> asks) {
        this.optionName = optionName;
        this.lastTradePrice = lastTradePrice;
        this.bestBid = bestBid;
        this.bestAsk = bestAsk;
        this.mid = mid;
        this.spread = spread;
        this.bids = bids;
        this.asks = asks;
    }

    public String optionName() {
        return optionName;
    }

    public Double lastTradePrice() {
        return lastTradePrice;
    }

    public Double bestBid() {
        return bestBid;
    }

    public Double bestAsk() {
        return bestAsk;
    }

    public Double mid() {
        return mid;
    }

    public Double spread() {
        return spread;
    }

    public List<RestingOrderView> bids() {
        return bids;
    }

    public List<RestingOrderView> asks() {
        return asks;
    }
}
