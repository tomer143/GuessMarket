package Engine;

import Engine.External.*;

import java.io.Serializable;
import java.util.List;

abstract class Event implements Serializable {
    protected int id;
    protected String mmUsername;
    protected String name;
    protected String description;
    protected int feePercent;
    protected FeeCollection feeCollection;
    protected List<Option> options;
    protected EventPhase phase = EventPhase.NOT_ACTIVE;
    protected double accountBalance;
    protected double totalFeeCollected;
    protected Option winningOption;

    protected abstract double getPrice(Option option, int amount) throws GuessMarketException;
    protected abstract double getOptionChance(Option option);
    protected abstract TradingMethod getTradingMethod();
    protected abstract void open(String requestingUsername) throws GuessMarketException;
    protected abstract void close(int winningOptionIndex, String requestingUsername) throws GuessMarketException;

    protected EventDetails getDetails() {
        List<String> optionNames = this.options.stream().map(Option::name).toList();

        return new EventDetails(this.id, this.name, this.description, this.feePercent, this.feeCollection, optionNames, this.phase, this.getTradingMethod(), this.mmUsername, this.accountBalance);
    }

    protected Option getOptionByIndex(int index) throws GuessMarketException {
        if (index < 0 || index >= this.options.size())
            throw new GuessMarketException("Invalid option number: " + (index + 1) + ".");

        return this.options.get(index);
    }

    protected Option getOtherOption(Option option) {
        return this.options.get(0).id() == option.id() ? this.options.get(1) : this.options.get(0);
    }

    protected void validateMm(String requestingUsername) throws GuessMarketException {
        User requester = Manager.getInstance().getUserByUsername(requestingUsername);

        if (requester.blocked())
            throw new GuessMarketException("User \"" + requestingUsername + "\" is blocked and cannot perform any actions.");

        if (!requestingUsername.equals(this.mmUsername))
            throw new GuessMarketException("Only \"" + this.mmUsername + "\" (the assigned market maker) can perform this action on event \"" + this.name + "\".");
    }
}
