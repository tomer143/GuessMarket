package Engine;

import Engine.External.FeeCollection;
import Engine.External.GuessMarketException;
import Engine.External.PurchaseResult;

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

    protected double getCreationCost() {
        return getCost(0, 0);
    }

    @Override
    protected PurchaseResult buy(int optionIndex, int amount) throws GuessMarketException {
        if (!this.isActive)
            throw new GuessMarketException("Event \"" + this.name + "\" is closed and cannot accept purchases.");
        if (amount <= 0)
            throw new GuessMarketException("The amount of shares must be a positive number.");

        Option option = getOptionByIndex(optionIndex);
        double sharesCost = getPrice(option, amount);
        double feeAmount = this.feeCollection == FeeCollection.OnPurchase ? this.feePercent / 100.0 * sharesCost : 0;

        this.accountBalance += sharesCost + feeAmount;
        this.totalFeeCollected += feeAmount;

        Purchase purchase = new Purchase(this.id, amount, sharesCost, option);
        Manager.getInstance().addPurchase(purchase);

        return new PurchaseResult(sharesCost, feeAmount, sharesCost + feeAmount);
    }

    @Override
    protected void close(int winningOptionIndex) throws GuessMarketException {
        if (!this.isActive)
            throw new GuessMarketException("Event \"" + this.name + "\" is already closed.");

        Option winner = getOptionByIndex(winningOptionIndex);
        int totalWinningShares = Manager.getInstance().getOptionTotalShares(this.id, winner.id());

        double feeAmount = this.feeCollection == FeeCollection.OnClose
                ? this.feePercent / 100.0 * totalWinningShares
                : 0;
        double payout = totalWinningShares - feeAmount;

        this.totalFeeCollected += feeAmount;
        this.accountBalance -= payout;
        this.isActive = false;
        this.winningOption = winner;
    }

    private Option getOptionByIndex(int index) throws GuessMarketException {
        if (index < 0 || index >= this.options.size())
            throw new GuessMarketException("Invalid option number: " + (index + 1) + ".");

        return this.options.get(index);
    }

    private Option getOtherOption(Option option) {
        return this.options.get(0).id() == option.id() ? this.options.get(1) : this.options.get(0);
    }

    private double getOptionRatio(Option option) {
        int optionShares = Manager.getInstance().getOptionTotalShares(this.id, option.id());

        return Math.exp(optionShares / this.instability);
    }

    private double getCost(double qA, double qB) {
        return this.instability * Math.log(Math.exp(qA / this.instability) + Math.exp(qB / this.instability));
    }
}
