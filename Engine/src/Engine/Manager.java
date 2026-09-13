package Engine;

import Engine.External.GuessMarketException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class Manager implements Serializable {
    private static Manager global;

    private List<Event> events;
    private List<Purchase> purchases;
    private List<User> users;
    private int nextOrderId;
    private int nextTradeId;

    private Manager() {
        this.events = new ArrayList<Event>();
        this.purchases = new ArrayList<Purchase>();
        this.users = new ArrayList<User>();
        this.nextOrderId = 0;
        this.nextTradeId = 0;
    }

    public static Manager getInstance() {
        if (global == null)
            global = new Manager();

        return global;
    }

    public static void restoreInstance(Manager restored) {
        global = restored;
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

    public List<User> getUsers() {
        return this.users;
    }

    public User getUserByUsername(String username) throws GuessMarketException {
        return this.users.stream()
                .filter(user -> user.username().equals(username))
                .findFirst()
                .orElseThrow(() -> new GuessMarketException("No user named \"" + username + "\" is currently loaded."));
    }

    public int nextOrderId() {
        return ++nextOrderId;
    }

    public int nextTradeId() {
        return ++nextTradeId;
    }

    public void addEvent(Event event) {
        this.events.add(event);
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

    public void replaceState(List<Event> newEvents, List<User> newUsers) {
        this.events = newEvents;
        this.users = newUsers;
        this.purchases = new ArrayList<>();
        this.nextOrderId = 0;
        this.nextTradeId = 0;
    }
}
