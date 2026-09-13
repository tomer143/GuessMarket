package Engine;

import Engine.External.*;

import java.util.ArrayList;
import java.util.List;

public class GuessMarketEngine {

    public void loadEventsFile(String path) throws GuessMarketException {
        EventFileLoader.load(path);
    }

    public List<EventDetails> getAllEvents() {
        return Manager.getInstance().getEvents().stream().map(Event::getDetails).toList();
    }

    public List<EventDetails> getActiveEvents() {
        return Manager.getInstance().getEvents().stream().filter(event -> event.phase == EventPhase.ACTIVE).map(Event::getDetails).toList();
    }

    public void openEvent(int eventId, String username) throws GuessMarketException {
        Manager.getInstance().getEventById(eventId).open(username);
    }

    public EventStatus getEventStatus(int eventId) throws GuessMarketException {
        Event event = Manager.getInstance().getEventById(eventId);

        if (!(event instanceof LmsrEvent lmsrEvent))
            throw new GuessMarketException("Event \"" + event.name + "\" does not use LMSR trading; use getOrderBookStatus instead.");

        return lmsrEvent.getStatus();
    }

    public PurchaseResult buyShares(int eventId, int optionIndex, int amount, String username) throws GuessMarketException {
        Event event = Manager.getInstance().getEventById(eventId);

        if (!(event instanceof LmsrEvent lmsrEvent))
            throw new GuessMarketException("Event \"" + event.name + "\" does not use LMSR trading; use submitOrder instead.");

        return lmsrEvent.buy(optionIndex, amount, username);
    }

    public EventStatus closeEvent(int eventId, int winningOptionIndex, String username) throws GuessMarketException {
        Event event = Manager.getInstance().getEventById(eventId);

        if (!(event instanceof LmsrEvent lmsrEvent))
            throw new GuessMarketException("Event \"" + event.name + "\" does not use LMSR trading; use closeOrderBookEvent instead.");

        lmsrEvent.close(winningOptionIndex, username);
        return lmsrEvent.getStatus();
    }

    public OrderResult submitOrder(int eventId, int optionIndex, OrderAction side, int quantity, double price, String username) throws GuessMarketException {
        Event event = Manager.getInstance().getEventById(eventId);

        if (!(event instanceof OrderBookEvent orderBookEvent))
            throw new GuessMarketException("Event \"" + event.name + "\" does not use order-book trading; use buyShares instead.");

        return orderBookEvent.submitOrder(optionIndex, side, quantity, price, username);
    }

    public OrderBookStatus getOrderBookStatus(int eventId) throws GuessMarketException {
        Event event = Manager.getInstance().getEventById(eventId);

        if (!(event instanceof OrderBookEvent orderBookEvent))
            throw new GuessMarketException("Event \"" + event.name + "\" does not use order-book trading; use getEventStatus instead.");

        return orderBookEvent.getOrderBookStatus();
    }

    public OrderBookStatus closeOrderBookEvent(int eventId, int winningOptionIndex, String username) throws GuessMarketException {
        Event event = Manager.getInstance().getEventById(eventId);

        if (!(event instanceof OrderBookEvent orderBookEvent))
            throw new GuessMarketException("Event \"" + event.name + "\" does not use order-book trading; use closeEvent instead.");

        orderBookEvent.close(winningOptionIndex, username);
        return orderBookEvent.getOrderBookStatus();
    }

    public List<HoldingSummary> getUserHoldings(int eventId, String username) throws GuessMarketException {
        Event event = Manager.getInstance().getEventById(eventId);

        if (!(event instanceof OrderBookEvent orderBookEvent))
            throw new GuessMarketException("Event \"" + event.name + "\" does not use order-book trading.");

        return orderBookEvent.getHoldings(username);
    }

    public LmsrParticipation getUserLmsrParticipation(int eventId, String username) throws GuessMarketException {
        Event event = Manager.getInstance().getEventById(eventId);

        if (!(event instanceof LmsrEvent lmsrEvent))
            throw new GuessMarketException("Event \"" + event.name + "\" does not use LMSR trading.");

        return lmsrEvent.getParticipation(username);
    }

    public OrderBookParticipation getUserOrderBookParticipation(int eventId, String username) throws GuessMarketException {
        Event event = Manager.getInstance().getEventById(eventId);

        if (!(event instanceof OrderBookEvent orderBookEvent))
            throw new GuessMarketException("Event \"" + event.name + "\" does not use order-book trading.");

        return orderBookEvent.getParticipation(username);
    }

    public List<ParticipantSummary> getOrderBookParticipants(int eventId) throws GuessMarketException {
        Event event = Manager.getInstance().getEventById(eventId);

        if (!(event instanceof OrderBookEvent orderBookEvent))
            throw new GuessMarketException("Event \"" + event.name + "\" does not use order-book trading.");

        return orderBookEvent.getParticipants();
    }

    public List<UserSummary> getAllUsers() {
        return Manager.getInstance().getUsers().stream()
                .map(user -> new UserSummary(user.username(), user.balance(), user.blocked()))
                .toList();
    }

    public UserDetails getUserDetails(String username) throws GuessMarketException {
        User user = Manager.getInstance().getUserByUsername(username);

        List<UserEventParticipation> participations = Manager.getInstance().getEvents().stream()
                .filter(event -> isParticipant(event, username))
                .map(event -> new UserEventParticipation(event.id, event.name, event.getTradingMethod(), username.equals(event.mmUsername)))
                .toList();

        return new UserDetails(user.username(), user.balance(), user.blocked(), participations);
    }

    private boolean isParticipant(Event event, String username) {
        if (username.equals(event.mmUsername))
            return true;
        if (event instanceof LmsrEvent)
            return Manager.getInstance().getPurchasesByEventId(event.id).stream().anyMatch(purchase -> purchase.username().equals(username));
        if (event instanceof OrderBookEvent orderBookEvent)
            return !orderBookEvent.getHoldings(username).isEmpty();

        return false;
    }

    public List<BalanceHistoryPoint> getUserBalanceHistory(String username) throws GuessMarketException {
        User user = Manager.getInstance().getUserByUsername(username);
        List<Double> history = user.balanceHistory();

        List<BalanceHistoryPoint> points = new ArrayList<>();
        for (int i = 0; i < history.size(); i++)
            points.add(new BalanceHistoryPoint(i, history.get(i)));

        return points;
    }

    public void saveState(String path) throws GuessMarketException {
        StateFileManager.save(path);
    }

    public void loadState(String path) throws GuessMarketException {
        StateFileManager.load(path);
    }
}
