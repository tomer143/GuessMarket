package Engine;

import Engine.External.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class LmsrEvent extends Event {
    protected double instability;

    @Override
    protected double getPrice(Option option, int amount) {
        Option otherOption = getOtherOption(option);
        int qBefore = Manager.getInstance().getOptionTotalShares(this.id, option.id());
        int qOtherBefore = Manager.getInstance().getOptionTotalShares(this.id, otherOption.id());

        return getCost(qBefore + amount, qOtherBefore) - getCost(qBefore, qOtherBefore);
    }

    @Override
    protected double getOptionChance(Option option) {
        Option other = getOtherOption(option);

        double optionRatio = this.getOptionRatio(option);
        double otherRatio = this.getOptionRatio(other);

        return optionRatio / (optionRatio + otherRatio);
    }

    @Override
    protected TradingMethod getTradingMethod() {
        return TradingMethod.LMSR;
    }

    protected double getCreationCost() {
        return getCost(0, 0);
    }

    @Override
    protected void open(String requestingUsername) throws GuessMarketException {
        validateMm(requestingUsername);

        if (this.phase != EventPhase.NOT_ACTIVE)
            throw new GuessMarketException("Event \"" + this.name + "\" has already been opened.");

        User requester = Manager.getInstance().getUserByUsername(requestingUsername);
        double subsidy = getCreationCost();

        if (requester.balance() < subsidy)
            throw new GuessMarketException("You do not have enough balance to open event \"" + this.name + "\" (requires " + subsidy + ").");

        requester.adjustBalance(-subsidy);
        this.accountBalance = subsidy;
        this.phase = EventPhase.ACTIVE;
    }

    protected PurchaseResult buy(int optionIndex, int amount, String username) throws GuessMarketException {
        if (this.phase != EventPhase.ACTIVE)
            throw new GuessMarketException("Event \"" + this.name + "\" is not active and cannot accept purchases.");

        if (amount <= 0)
            throw new GuessMarketException("The amount of shares must be a positive number.");

        User buyer = Manager.getInstance().getUserByUsername(username);
        if (buyer.blocked())
            throw new GuessMarketException("User \"" + username + "\" is blocked and cannot perform any actions.");

        Option option = getOptionByIndex(optionIndex);
        double sharesCost = getPrice(option, amount);
        double feeAmount = this.feeCollection == FeeCollection.OnPurchase ? this.feePercent / 100.0 * sharesCost : 0;

        buyer.adjustBalance(-(sharesCost + feeAmount));
        this.accountBalance += sharesCost + feeAmount;
        this.totalFeeCollected += feeAmount;

        Purchase purchase = new Purchase(this.id, amount, sharesCost, feeAmount, option, username);
        Manager.getInstance().addPurchase(purchase);

        return new PurchaseResult(sharesCost, feeAmount, sharesCost + feeAmount);
    }

    @Override
    protected void close(int winningOptionIndex, String requestingUsername) throws GuessMarketException {
        validateMm(requestingUsername);

        if (this.phase != EventPhase.ACTIVE)
            throw new GuessMarketException("Event \"" + this.name + "\" is not active and cannot be closed.");

        Option winner = getOptionByIndex(winningOptionIndex);
        int totalWinningShares = Manager.getInstance().getOptionTotalShares(this.id, winner.id());

        double feeAmount = this.feeCollection == FeeCollection.OnClose
                ? this.feePercent / 100.0 * totalWinningShares
                : 0;
        double payout = totalWinningShares - feeAmount;

        this.totalFeeCollected += feeAmount;

        Map<String, Integer> winnerShares = Manager.getInstance().getPurchasesByEventId(this.id).stream()
                .filter(purchase -> purchase.option().id() == winner.id())
                .collect(Collectors.groupingBy(Purchase::username, Collectors.summingInt(Purchase::amount)));

        for (Map.Entry<String, Integer> entry : winnerShares.entrySet()) {
            double holderPayout = entry.getValue() * (1 - (this.feeCollection == FeeCollection.OnClose ? this.feePercent / 100.0 : 0));
            Manager.getInstance().getUserByUsername(entry.getKey()).adjustBalance(holderPayout);
        }

        double mmRefund = this.accountBalance - payout;
        Manager.getInstance().getUserByUsername(this.mmUsername).adjustBalance(mmRefund);
        this.accountBalance = 0;

        this.phase = EventPhase.CLOSED;
        this.winningOption = winner;
    }

    protected EventStatus getStatus() {
        List<OptionStatus> optionStatuses = this.options.stream()
                .map(option -> new OptionStatus(option.name(), this.getOptionChance(option), Manager.getInstance().getOptionTotalShares(this.id, option.id())))
                .toList();

        List<TradeRecord> history = new ArrayList<>(Manager.getInstance().getPurchasesByEventId(this.id).stream()
                .map(purchase -> new TradeRecord(purchase.option().name(), purchase.amount(), purchase.price()))
                .toList());
        Collections.reverse(history);

        String winningOptionName = this.winningOption != null ? this.winningOption.name() : null;

        return new EventStatus(this.id, this.name, this.phase, optionStatuses, this.accountBalance, this.totalFeeCollected, history, winningOptionName);
    }

    protected LmsrParticipation getParticipation(String username) {
        List<Purchase> ownPurchases = Manager.getInstance().getPurchasesByEventId(this.id).stream()
                .filter(purchase -> purchase.username().equals(username))
                .toList();

        List<TradeRecord> history = new ArrayList<>(ownPurchases.stream()
                .map(purchase -> new TradeRecord(purchase.option().name(), purchase.amount(), purchase.price()))
                .toList());
        Collections.reverse(history);

        double totalFeePaid = ownPurchases.stream().mapToDouble(Purchase::feeAmount).sum();

        List<OptionStatus> optionStatuses = this.options.stream()
                .map(option -> new OptionStatus(option.name(), this.getOptionChance(option), Manager.getInstance().getOptionTotalShares(this.id, option.id())))
                .toList();

        String winningOptionName = this.winningOption != null ? this.winningOption.name() : null;

        return new LmsrParticipation(history, totalFeePaid, optionStatuses, this.phase, winningOptionName);
    }

    private double getOptionRatio(Option option) {
        int optionShares = Manager.getInstance().getOptionTotalShares(this.id, option.id());

        return Math.exp(optionShares / this.instability);
    }

    private double getCost(double qA, double qB) {
        return this.instability * Math.log(Math.exp(qA / this.instability) + Math.exp(qB / this.instability));
    }
}
