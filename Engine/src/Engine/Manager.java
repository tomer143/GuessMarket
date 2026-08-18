package Engine;

import Engine.External.GuessMarketException;

import java.util.ArrayList;
import java.util.List;

class Manager {
    private static Manager global;

    private List<Event> events;
    private List<Purchase> purchases;
    private MoneyMaker moneyMaker;

    private Manager() {
        this.events = new ArrayList<Event>();
        this.purchases = new ArrayList<Purchase>();
        this.moneyMaker = new MoneyMaker(1, 0);
    }

    public static Manager getInstance() {
        if (global == null)
            global = new Manager();

        return global;
    }

    public List<Event> getEvents() {
        return this.events;
    }

    public Event getEventById(int id) throws GuessMarketException {
        return this.events.stream()
                .filter(event -> event.id == id)
                .findFirst()
                .orElseThrow(() -> new GuessMarketException("No event with id " + id + " is currently loaded."));
    }

    public MoneyMaker getMoneyMaker() {
        return this.moneyMaker;
    }

    public void addPurchase(Purchase purchase) {
        this.purchases.add(purchase);
    }

    public List<Purchase> getPurchasesByEventId(int eventId) {
        return this.purchases.stream().filter(purchase -> purchase.eventId() == eventId).toList();
    }

    public int getOptionTotalShares(int eventId, int optionId) {
        return this.purchases.stream()
                .filter(purchase -> purchase.eventId() == eventId && purchase.option().id() == optionId)
                .mapToInt(Purchase::amount)
                .sum();
    }

    public void replaceState(List<Event> newEvents, MoneyMaker newMoneyMaker) {
        this.events = newEvents;
        this.moneyMaker = newMoneyMaker;
        this.purchases = new ArrayList<>();
    }
}
