package Engine;

import Engine.External.*;

import java.util.List;

public class GuessMarketEngine {

    public void loadEventsFile(String path) throws GuessMarketException {
        EventFileLoader.load(path);
    }

    public List<EventDetails> getAllEvents() {
        return Manager.getInstance().getEvents().stream().map(Event::getDetails).toList();
    }

    public List<EventDetails> getActiveEvents() {
        return Manager.getInstance().getEvents().stream().filter(event -> event.isActive).map(Event::getDetails).toList();
    }

    public EventStatus getEventStatus(int eventId) throws GuessMarketException {
        return Manager.getInstance().getEventById(eventId).getStatus();
    }

    public PurchaseResult buyShares(int eventId, int optionIndex, int amount) throws GuessMarketException {
        return Manager.getInstance().getEventById(eventId).buy(optionIndex, amount);
    }

    public EventStatus closeEvent(int eventId, int winningOptionIndex) throws GuessMarketException {
        Event event = Manager.getInstance().getEventById(eventId);
        event.close(winningOptionIndex);

        return event.getStatus();
    }
}
