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

    public int createLmsrEvent(String name, String description, int feePercent, FeeCollection feeCollection,
                                String optionAName, String optionBName, int liquidityB, String creatorUsername) throws GuessMarketException {
        if (liquidityB <= 0)
            throw new GuessMarketException("The liquidity parameter (b) must be a positive number.");

        LmsrEvent event = new LmsrEvent();
        event.instability = liquidityB;

        return createEvent(event, name, description, feePercent, feeCollection, optionAName, optionBName, creatorUsername);
    }

    public int createOrderBookEvent(String name, String description, int feePercent, FeeCollection feeCollection,
                                     String optionAName, String optionBName, int baseValue, int initialAmount, boolean allowMint,
                                     String creatorUsername) throws GuessMarketException {
        if (baseValue <= 0)
            throw new GuessMarketException("The base value (d) must be a positive number.");
        if (initialAmount < 0)
            throw new GuessMarketException("The initial amount cannot be negative.");

        OrderBookEvent event = new OrderBookEvent();
        event.baseValue = baseValue;
        event.initialAmount = initialAmount;
        event.allowMint = allowMint;
        event.books = new ArrayList<>();
        event.holdings = new ArrayList<>();
        event.trades = new ArrayList<>();

        int eventId = createEvent(event, name, description, feePercent, feeCollection, optionAName, optionBName, creatorUsername);

        for (Option option : event.options)
            event.books.add(new OrderBook(option.id()));

        return eventId;
    }

    private int createEvent(Event event, String name, String description, int feePercent, FeeCollection feeCollection,
                             String optionAName, String optionBName, String creatorUsername) throws GuessMarketException {
        if (name == null || name.isBlank())
            throw new GuessMarketException("The event name cannot be blank.");
        if (description == null || description.isBlank())
            throw new GuessMarketException("The event description cannot be blank.");
        if (feePercent < 0 || feePercent > 90)
            throw new GuessMarketException("The fee must be between 0 and 90 percent.");
        if (feeCollection == null)
            throw new GuessMarketException("A fee collection method must be specified.");
        if (optionAName == null || optionAName.isBlank() || optionBName == null || optionBName.isBlank())
            throw new GuessMarketException("Both option names must be provided.");
        if (optionAName.trim().equalsIgnoreCase(optionBName.trim()))
            throw new GuessMarketException("The two options must have different names.");

        User creator = Manager.getInstance().getUserByUsername(creatorUsername);
        if (creator.blocked())
            throw new GuessMarketException("User \"" + creatorUsername + "\" is blocked and cannot perform any actions.");

        int newId = Manager.getInstance().getEvents().stream().mapToInt(existing -> existing.id).max().orElse(0) + 1;

        event.id = newId;
        event.name = name.trim();
        event.description = description.trim();
        event.feePercent = feePercent;
        event.feeCollection = feeCollection;
        event.options = List.of(new Option(1, optionAName.trim()), new Option(2, optionBName.trim()));
        event.phase = EventPhase.NOT_ACTIVE;
        event.mmUsername = creatorUsername;

        Manager.getInstance().addEvent(event);

        return newId;
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
        boolean mmHasActed = username.equals(event.mmUsername) && event.phase != EventPhase.NOT_ACTIVE;

        if (event instanceof LmsrEvent)
            return mmHasActed || Manager.getInstance().getPurchasesByEventId(event.id).stream().anyMatch(purchase -> purchase.username().equals(username));
        if (event instanceof OrderBookEvent orderBookEvent)
            return mmHasActed || orderBookEvent.getParticipants().stream().anyMatch(participant -> participant.username().equals(username));

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
