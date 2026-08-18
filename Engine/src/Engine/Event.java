package Engine;

import Engine.External.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class Event implements Serializable {
    protected int id;
    protected int mmId;
    protected String name;
    protected String description;
    protected int feePercent;
    protected FeeCollection feeCollection;
    protected List<Option> options;
    protected boolean isActive;
    protected double accountBalance;
    protected double totalFeeCollected;
    protected Option winningOption;

    protected abstract double getPrice(Option option, int amount);
    protected abstract double getOptionChance(Option option);
    protected abstract PurchaseResult buy(int optionIndex, int amount) throws GuessMarketException;
    protected abstract void close(int winningOptionIndex) throws GuessMarketException;

    protected EventDetails getDetails() {
        List<String> optionNames = this.options.stream().map(Option::name).toList();

        return new EventDetails(this.id, this.name, this.description, this.feePercent, this.feeCollection, optionNames, this.isActive);
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

        return new EventStatus(this.id, this.name, this.isActive, optionStatuses, this.accountBalance, this.totalFeeCollected, history, winningOptionName);
    }
}
