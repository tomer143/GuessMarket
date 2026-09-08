package Engine;

import Engine.External.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class OrderBookEvent extends Event {
    protected int baseValue;
    protected int initialAmount;
    protected boolean allowMint;
    protected List<OrderBook> books;
    protected List<Holding> holdings;
    protected List<Trade> trades;
    protected Map<String, Double> participantFeesPaid = new HashMap<>();
    protected Map<String, Double> participantNetFlow = new HashMap<>();

    @Override
    protected double getPrice(Option option, int amount) throws GuessMarketException {
        OrderBook book = getBookForOption(option);
        List<Order> asks = book.asksBestFirst();

        int remaining = amount;
        double total = 0;
        int i = 0;
        while (remaining > 0 && i < asks.size()) {
            Order ask = asks.get(i);
            int qty = Math.min(remaining, ask.remainingQuantity());
            total += qty * ask.price();
            remaining -= qty;
            i++;
        }

        if (remaining > 0)
            throw new GuessMarketException("Not enough resting supply to quote a price for " + amount + " shares of \"" + option.name() + "\".");

        return total;
    }

    @Override
    protected double getOptionChance(Option option) {
        OrderBook book = getBookForOption(option);
        Double bestBid = book.bestBid();
        Double bestAsk = book.bestAsk();

        if (bestBid != null && bestAsk != null)
            return (bestBid + bestAsk) / 2.0 / this.baseValue;
        if (book.lastTradePrice() != null)
            return book.lastTradePrice() / this.baseValue;

        return 0.5;
    }

    @Override
    protected TradingMethod getTradingMethod() {
        return TradingMethod.ORDER_BOOK;
    }

    @Override
    protected void open(String requestingUsername) throws GuessMarketException {
        validateMm(requestingUsername);

        if (this.phase != EventPhase.NOT_ACTIVE)
            throw new GuessMarketException("Event \"" + this.name + "\" has already been opened.");

        User requester = Manager.getInstance().getUserByUsername(requestingUsername);
        if (requester.balance() < this.initialAmount)
            throw new GuessMarketException("You do not have enough balance to open event \"" + this.name + "\" (requires " + this.initialAmount + ").");

        requester.adjustBalance(-this.initialAmount);
        this.accountBalance = this.initialAmount;
        participantNetFlow.merge(requestingUsername, (double) -this.initialAmount, Double::sum);

        int pairQuantity = this.initialAmount / this.baseValue;
        if (pairQuantity > 0) {
            for (Option option : this.options) {
                Holding holding = getOrCreateHolding(requestingUsername, option);
                holding.applyBuy(pairQuantity, this.initialAmount / 2.0);
                getBookForOption(option).increaseSupply(pairQuantity);
            }
        }

        this.phase = EventPhase.ACTIVE;
    }

    protected OrderResult submitOrder(int optionIndex, OrderAction side, int quantity, double price, String username) throws GuessMarketException {
        if (this.phase != EventPhase.ACTIVE)
            throw new GuessMarketException("Event \"" + this.name + "\" is not active and cannot accept orders.");

        if (quantity <= 0)
            throw new GuessMarketException("The quantity must be a positive number.");

        if (price <= 0 || price > this.baseValue - 0.01)
            throw new GuessMarketException("The price must be greater than 0 and at most " + (this.baseValue - 0.01) + ".");

        Option option = getOptionByIndex(optionIndex);

        User account = Manager.getInstance().getUserByUsername(username);
        if (account.blocked())
            throw new GuessMarketException("User \"" + username + "\" is blocked and cannot perform any actions.");

        if (side == OrderAction.SELL)
            validateSellable(username, option, quantity);

        List<FillRecord> fills = new ArrayList<>();
        int remaining = quantity;

        remaining = matchSameOption(option, side, price, remaining, username, fills);

        if (side == OrderAction.BUY && this.allowMint && remaining > 0)
            remaining = matchMint(option, price, remaining, username, fills);

        if (remaining > 0) {
            Order restingOrder = new Order(Manager.getInstance().nextOrderId(), this.id, option.id(), side, username, price, remaining);
            OrderBook book = getBookForOption(option);

            if (side == OrderAction.BUY) book.addBid(restingOrder);
            else book.addAsk(restingOrder);
        }

        return new OrderResult(fills, remaining);
    }

    private int matchSameOption(Option option, OrderAction side, double price, int remaining, String username, List<FillRecord> fills) throws GuessMarketException {
        OrderBook book = getBookForOption(option);
        List<Order> candidates = side == OrderAction.BUY ? book.asksBestFirst() : book.bidsBestFirst();

        int i = 0;
        while (remaining > 0 && i < candidates.size()
                && (side == OrderAction.BUY ? candidates.get(i).price() <= price : candidates.get(i).price() >= price)) {
            Order resting = candidates.get(i);

            int tradeQty = Math.min(remaining, resting.remainingQuantity());
            double tradePrice = resting.price();

            String buyerUsername = side == OrderAction.BUY ? username : resting.username();
            String sellerUsername = side == OrderAction.BUY ? resting.username() : username;

            executeSameOptionTrade(option, buyerUsername, sellerUsername, tradeQty, tradePrice);

            double feeAmount = this.feeCollection == FeeCollection.OnPurchase ? this.feePercent / 100.0 * tradeQty * tradePrice : 0;
            fills.add(new FillRecord(tradeQty, tradePrice, side == OrderAction.BUY ? feeAmount : 0, false, resting.username()));

            resting.fill(tradeQty);
            book.removeIfFilled(resting);
            remaining -= tradeQty;
            i++;
        }

        return remaining;
    }

    private void executeSameOptionTrade(Option option, String buyerUsername, String sellerUsername, int quantity, double price) throws GuessMarketException {
        User buyer = Manager.getInstance().getUserByUsername(buyerUsername);
        User seller = Manager.getInstance().getUserByUsername(sellerUsername);

        double tradeValue = quantity * price;
        double feeAmount = this.feeCollection == FeeCollection.OnPurchase ? this.feePercent / 100.0 * tradeValue : 0;

        buyer.adjustBalance(-(tradeValue + feeAmount));
        seller.adjustBalance(tradeValue);
        this.accountBalance += feeAmount;
        this.totalFeeCollected += feeAmount;
        participantNetFlow.merge(buyerUsername, -(tradeValue + feeAmount), Double::sum);
        participantNetFlow.merge(sellerUsername, tradeValue, Double::sum);
        if (feeAmount > 0) participantFeesPaid.merge(buyerUsername, feeAmount, Double::sum);

        getOrCreateHolding(buyerUsername, option).applyBuy(quantity, tradeValue);
        getOrCreateHolding(sellerUsername, option).applySell(quantity, tradeValue);

        OrderBook book = getBookForOption(option);
        book.recordTrade(price);
        this.trades.add(new Trade(Manager.getInstance().nextTradeId(), this.id, option, buyerUsername, sellerUsername, quantity, price, false));
    }

    private int matchMint(Option option, double price, int remaining, String username, List<FillRecord> fills) throws GuessMarketException {
        Option other = getOtherOption(option);
        OrderBook otherBook = getBookForOption(other);
        List<Order> candidates = otherBook.bidsBestFirst();

        int i = 0;
        while (remaining > 0 && i < candidates.size() && price + candidates.get(i).price() >= this.baseValue) {
            Order resting = candidates.get(i);

            int mintQty = Math.min(remaining, resting.remainingQuantity());
            double priceA = this.baseValue - resting.price();
            double priceB = resting.price();

            executeMint(option, other, username, resting.username(), mintQty, priceA, priceB);

            double feeA = this.feeCollection == FeeCollection.OnPurchase ? this.feePercent / 100.0 * mintQty * priceA : 0;
            fills.add(new FillRecord(mintQty, priceA, feeA, true, resting.username()));

            resting.fill(mintQty);
            otherBook.removeIfFilled(resting);
            remaining -= mintQty;
            i++;
        }

        return remaining;
    }

    private void executeMint(Option option, Option other, String incomingUsername, String restingUsername, int quantity, double priceA, double priceB) throws GuessMarketException {
        User incomingAccount = Manager.getInstance().getUserByUsername(incomingUsername);
        User restingAccount = Manager.getInstance().getUserByUsername(restingUsername);

        double feeA = this.feeCollection == FeeCollection.OnPurchase ? this.feePercent / 100.0 * quantity * priceA : 0;
        double feeB = this.feeCollection == FeeCollection.OnPurchase ? this.feePercent / 100.0 * quantity * priceB : 0;

        incomingAccount.adjustBalance(-(quantity * priceA + feeA));
        restingAccount.adjustBalance(-(quantity * priceB + feeB));
        this.accountBalance += quantity * priceA + quantity * priceB + feeA + feeB;
        this.totalFeeCollected += feeA + feeB;
        participantNetFlow.merge(incomingUsername, -(quantity * priceA + feeA), Double::sum);
        participantNetFlow.merge(restingUsername, -(quantity * priceB + feeB), Double::sum);
        if (feeA > 0) participantFeesPaid.merge(incomingUsername, feeA, Double::sum);
        if (feeB > 0) participantFeesPaid.merge(restingUsername, feeB, Double::sum);

        getOrCreateHolding(incomingUsername, option).applyBuy(quantity, quantity * priceA);
        getOrCreateHolding(restingUsername, other).applyBuy(quantity, quantity * priceB);

        OrderBook book = getBookForOption(option);
        OrderBook otherBook = getBookForOption(other);
        book.increaseSupply(quantity);
        otherBook.increaseSupply(quantity);
        book.recordTrade(priceA);
        otherBook.recordTrade(priceB);

        this.trades.add(new Trade(Manager.getInstance().nextTradeId(), this.id, option, incomingUsername, null, quantity, priceA, true));
        this.trades.add(new Trade(Manager.getInstance().nextTradeId(), this.id, other, restingUsername, null, quantity, priceB, true));
    }

    @Override
    protected void close(int winningOptionIndex, String requestingUsername) throws GuessMarketException {
        validateMm(requestingUsername);
        if (this.phase != EventPhase.ACTIVE)
            throw new GuessMarketException("Event \"" + this.name + "\" is not active and cannot be closed.");

        Option winner = getOptionByIndex(winningOptionIndex);

        for (Holding holding : this.holdings) {
            if (holding.optionId() != winner.id() || holding.quantity() <= 0) continue;

            double grossPayout = holding.quantity() * this.baseValue;
            double feeAmount = this.feeCollection == FeeCollection.OnClose ? this.feePercent / 100.0 * grossPayout : 0;
            double netPayout = grossPayout - feeAmount;

            this.totalFeeCollected += feeAmount;
            this.accountBalance -= netPayout;
            Manager.getInstance().getUserByUsername(holding.username()).adjustBalance(netPayout);
            participantNetFlow.merge(holding.username(), netPayout, Double::sum);
            if (feeAmount > 0) participantFeesPaid.merge(holding.username(), feeAmount, Double::sum);
        }

        Manager.getInstance().getUserByUsername(this.mmUsername).adjustBalance(this.accountBalance);
        participantNetFlow.merge(this.mmUsername, this.accountBalance, Double::sum);
        this.accountBalance = 0;

        this.phase = EventPhase.CLOSED;
        this.winningOption = winner;
    }

    protected OrderBookStatus getOrderBookStatus() {
        List<OptionMarketData> optionData = this.options.stream().map(option -> {
            OrderBook book = getBookForOption(option);
            Double bestBid = book.bestBid();
            Double bestAsk = book.bestAsk();
            Double mid = (bestBid != null && bestAsk != null) ? (bestBid + bestAsk) / 2.0 : null;
            Double spread = (bestBid != null && bestAsk != null) ? bestAsk - bestBid : null;

            List<RestingOrderView> bids = book.bidsBestFirst().stream()
                    .map(order -> new RestingOrderView(order.username(), order.remainingQuantity(), order.price())).toList();
            List<RestingOrderView> asks = book.asksBestFirst().stream()
                    .map(order -> new RestingOrderView(order.username(), order.remainingQuantity(), order.price())).toList();

            return new OptionMarketData(option.name(), book.lastTradePrice(), bestBid, bestAsk, mid, spread, bids, asks);
        }).toList();

        List<TradeHistoryRecord> history = new ArrayList<>(this.trades.stream()
                .map(trade -> new TradeHistoryRecord(trade.option().name(), trade.buyerUsername(), trade.sellerUsername(), trade.quantity(), trade.price(), trade.minted()))
                .toList());
        Collections.reverse(history);

        String winningOptionName = this.winningOption != null ? this.winningOption.name() : null;

        return new OrderBookStatus(this.id, this.name, this.phase, this.baseValue, optionData, this.accountBalance, this.totalFeeCollected, history, winningOptionName);
    }

    protected List<HoldingSummary> getHoldings(String username) {
        return this.holdings.stream()
                .filter(holding -> holding.username().equals(username) && holding.quantity() > 0)
                .map(holding -> {
                    String optionName = this.options.stream().filter(o -> o.id() == holding.optionId()).findFirst().orElseThrow().name();
                    return new HoldingSummary(optionName, holding.quantity(), holding.amountPaid());
                })
                .toList();
    }

    protected OrderBookParticipation getParticipation(String username) {
        List<HoldingSummary> holdingSummaries = getHoldings(username);
        double totalFeePaid = participantFeesPaid.getOrDefault(username, 0.0);
        Double profitLoss = this.phase == EventPhase.CLOSED ? participantNetFlow.getOrDefault(username, 0.0) : null;

        return new OrderBookParticipation(holdingSummaries, totalFeePaid, profitLoss);
    }

    protected List<ParticipantSummary> getParticipants() {
        Set<String> usernames = new HashSet<>();

        for (Holding holding : this.holdings)
            if (holding.quantity() > 0) usernames.add(holding.username());

        for (OrderBook book : this.books) {
            for (Order order : book.bids()) usernames.add(order.username());
            for (Order order : book.asks()) usernames.add(order.username());
        }

        return usernames.stream()
                .map(username -> new ParticipantSummary(username, getHoldings(username)))
                .toList();
    }

    private OrderBook getBookForOption(Option option) {
        return this.books.stream().filter(book -> book.optionId() == option.id()).findFirst().orElseThrow();
    }

    private Holding getOrCreateHolding(String username, Option option) {
        return this.holdings.stream()
                .filter(holding -> holding.username().equals(username) && holding.optionId() == option.id())
                .findFirst()
                .orElseGet(() -> {
                    Holding holding = new Holding(this.id, option.id(), username);
                    this.holdings.add(holding);
                    return holding;
                });
    }

    private void validateSellable(String username, Option option, int quantity) throws GuessMarketException {
        Holding holding = getOrCreateHolding(username, option);
        OrderBook book = getBookForOption(option);
        int alreadyResting = book.asks().stream()
                .filter(order -> order.username().equals(username))
                .mapToInt(Order::remainingQuantity)
                .sum();

        if (quantity > holding.quantity() - alreadyResting)
            throw new GuessMarketException("\"" + username + "\" does not have enough unreserved shares of \"" + option.name() + "\" to sell " + quantity + ".");
    }
}
