package Engine;

import Engine.External.FeeCollection;
import Engine.External.GuessMarketException;
import Engine.Xml.Commission;
import Engine.Xml.GMEvent;
import Engine.Xml.GMLMSR;
import Engine.Xml.GMMethod;
import Engine.Xml.GMOptions;
import Engine.Xml.GMOrderBook;
import Engine.Xml.GMUser;
import Engine.Xml.GuessMarket;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class EventFileLoader {
    private static final String GENERATED_PACKAGE_NAME = "Engine.Xml";

    public static void load(String path) throws GuessMarketException {
        validatePath(path);

        GuessMarket guessMarket = parseDocument(path);
        if (guessMarket == null)
            throw new GuessMarketException("The file could not be parsed as valid XML.");

        if (guessMarket.getGMUsers() == null)
            throw new GuessMarketException("The file does not contain a GM-users section.");

        if (guessMarket.getGMEvents() == null)
            throw new GuessMarketException("The file does not contain a GM-events section.");

        List<GMUser> userElements = guessMarket.getGMUsers().getGMUser();
        if (userElements.isEmpty())
            throw new GuessMarketException("The file does not contain any users.");

        List<GMEvent> eventElements = guessMarket.getGMEvents().getGMEvent();
        if (eventElements.isEmpty())
            throw new GuessMarketException("The file does not contain any events.");

        List<User> users = new ArrayList<>();
        Set<String> existingUsernames = new HashSet<>();
        Map<Integer, String> mmAssignments = new HashMap<>();

        for (GMUser userElement : userElements) {
            if (userElement == null)
                throw new GuessMarketException("The file contains an empty user entry.");

            User user = parseUser(userElement, mmAssignments);

            if (existingUsernames.contains(user.username()))
                throw new GuessMarketException("Duplicate username \"" + user.username() + "\" found in the file.");

            existingUsernames.add(user.username());
            users.add(user);
        }

        List<Event> events = new ArrayList<>();
        Set<Integer> existingIds = new HashSet<>();

        for (GMEvent eventElement : eventElements) {
            if (eventElement == null)
                throw new GuessMarketException("The file contains an empty event entry.");

            Event event = parseEvent(eventElement);

            if (existingIds.contains(event.id))
                throw new GuessMarketException("Duplicate event id " + event.id + " found in the file.");

            String mmUsername = mmAssignments.remove(event.id);
            if (mmUsername == null)
                throw new GuessMarketException("Event \"" + event.name + "\" (id " + event.id + ") has no market maker assigned to it.");
            if (!existingUsernames.contains(mmUsername))
                throw new GuessMarketException("Event \"" + event.name + "\" (id " + event.id + ") is assigned to an unknown user \"" + mmUsername + "\".");
            event.mmUsername = mmUsername;

            existingIds.add(event.id);
            events.add(event);
        }

        if (!mmAssignments.isEmpty())
            throw new GuessMarketException("A user is assigned as market maker of event id " + mmAssignments.keySet().iterator().next() + ", which does not exist in this file.");

        Manager.getInstance().replaceState(events, users);
    }

    private static void validatePath(String path) throws GuessMarketException {
        if (path == null || path.isBlank())
            throw new GuessMarketException("No file path was provided.");

        if (!path.toLowerCase().endsWith(".xml"))
            throw new GuessMarketException("The file must have an \".xml\" extension.");

        File file = new File(path);
        if (!file.isFile())
            throw new GuessMarketException("The file \"" + path + "\" does not exist.");
    }

    private static GuessMarket parseDocument(String path) throws GuessMarketException {
        try {
            JAXBContext context = JAXBContext.newInstance(GENERATED_PACKAGE_NAME);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (GuessMarket) unmarshaller.unmarshal(new File(path));
        } catch (JAXBException exception) {
            throw new GuessMarketException("The file could not be parsed as valid XML: " + getParsingErrorMessage(exception));
        } catch (ClassCastException exception) {
            throw new GuessMarketException("The file's root element is not a Guess-Market document.");
        }
    }

    private static User parseUser(GMUser userElement, Map<Integer, String> mmAssignments) throws GuessMarketException {
        try {
            String name = userElement.getName();
            if (name == null || name.isBlank())
                throw new GuessMarketException("The file contains a user with a missing or blank name.");
            
            String username = name.trim();

            int initialCash = userElement.getInitialCash();
            if (initialCash <= 0)
                throw new GuessMarketException("User \"" + username + "\" must have an initial cash balance greater than 0.");

            if (userElement.getGMMarketMaker() != null) {
                for (Engine.Xml.Event eventRef : userElement.getGMMarketMaker().getEvent()) {
                    int eventId = eventRef.getId();
                    if (mmAssignments.containsKey(eventId))
                        throw new GuessMarketException("Event id " + eventId + " has more than one market maker assigned to it.");
                    mmAssignments.put(eventId, username);
                }
            }

            return new User(username, initialCash);
        } catch (GuessMarketException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new GuessMarketException("The file contains a user with missing or malformed data: " + exception.getMessage());
        }
    }

    private static Event parseEvent(GMEvent eventElement) throws GuessMarketException {
        try {
            String name = eventElement.getName();
            if (name == null || name.isBlank())
                throw new GuessMarketException("The file contains an event with a missing or blank name.");
            String eventName = name.trim();

            int id = eventElement.getId();
            if (id == 0)
                throw new GuessMarketException("Event \"" + eventName + "\" is missing a valid numeric id.");

            String description = eventElement.getDescription();
            if (description == null)
                throw new GuessMarketException("Event \"" + eventName + "\" is missing a description.");

            Commission commission = eventElement.getCommission();
            if (commission == null)
                throw new GuessMarketException("Event \"" + eventName + "\" is missing a valid commission value.");

            int feePercent = commission.getValue();
            if (feePercent < 0 || feePercent > 90)
                throw new GuessMarketException("Event \"" + eventName + "\" has an invalid fee of " + feePercent + "% (must be between 0 and 90).");

            String feeType = commission.getType();
            if (feeType == null || feeType.isBlank())
                throw new GuessMarketException("Event \"" + eventName + "\" is missing a valid commission type.");
            feeType = feeType.trim();

            FeeCollection feeCollection;
            if (feeType.equalsIgnoreCase("on-close"))
                feeCollection = FeeCollection.OnClose;
            else if (feeType.equalsIgnoreCase("on-purchase"))
                feeCollection = FeeCollection.OnPurchase;
            else
                throw new GuessMarketException("Event \"" + eventName + "\" has an unknown fee collection type \"" + feeType + "\".");

            GMOptions gmOptions = eventElement.getGMOptions();
            if (gmOptions == null)
                throw new GuessMarketException("Event \"" + eventName + "\" is missing a GM-options section.");

            List<String> optionNames = gmOptions.getGMOption();
            if (optionNames.size() != 2)
                throw new GuessMarketException("Event \"" + eventName + "\" must have exactly 2 options (found " + optionNames.size() + ").");

            List<Option> options = new ArrayList<>();
            for (int i = 0; i < optionNames.size(); i++) {
                String optionName = optionNames.get(i);
                if (optionName == null || optionName.isBlank())
                    throw new GuessMarketException("Event \"" + eventName + "\" has an option with a missing or blank name.");
                options.add(new Option(i + 1, optionName.trim()));
            }

            GMMethod gmMethod = eventElement.getGMMethod();
            if (gmMethod == null)
                throw new GuessMarketException("Event \"" + eventName + "\" is missing a GM-method section.");

            Event event;
            if (gmMethod.getGMLMSR() != null) {
                event = parseLmsrMethod(eventName, gmMethod.getGMLMSR());
            } else if (gmMethod.getGMOrderBook() != null) {
                event = parseOrderBookMethod(eventName, gmMethod.getGMOrderBook());
            } else {
                throw new GuessMarketException("Event \"" + eventName + "\" is missing a GM-LMSR or GM-order-book section.");
            }

            event.id = id;
            event.name = eventName;
            event.description = description.trim();
            event.feePercent = feePercent;
            event.feeCollection = feeCollection;
            event.options = options;
            event.phase = Engine.External.EventPhase.NOT_ACTIVE;

            if (event instanceof OrderBookEvent orderBookEvent) {
                for (Option option : options)
                    orderBookEvent.books.add(new OrderBook(option.id()));
            }

            return event;
        } catch (GuessMarketException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new GuessMarketException("The file contains an event with missing or malformed data: " + exception.getMessage());
        }
    }

    private static LmsrEvent parseLmsrMethod(String eventName, GMLMSR gmlmsr) throws GuessMarketException {
        int b = gmlmsr.getB();
        if (b <= 0)
            throw new GuessMarketException("Event \"" + eventName + "\" has an invalid liquidity value (b), it must be a positive number.");

        LmsrEvent event = new LmsrEvent();
        event.instability = b;
        return event;
    }

    private static OrderBookEvent parseOrderBookMethod(String eventName, GMOrderBook gmOrderBook) throws GuessMarketException {
        int d = gmOrderBook.getD();
        if (d <= 0)
            throw new GuessMarketException("Event \"" + eventName + "\" has an invalid base value (d), it must be a positive number.");

        int initial = gmOrderBook.getInitial();
        if (initial < 0)
            throw new GuessMarketException("Event \"" + eventName + "\" has an invalid initial amount, it cannot be negative.");

        String allowMintText = gmOrderBook.getAllowMint();
        if (!"true".equals(allowMintText) && !"false".equals(allowMintText))
            throw new GuessMarketException("Event \"" + eventName + "\" has an invalid allow-mint value \"" + allowMintText + "\".");

        OrderBookEvent event = new OrderBookEvent();
        event.baseValue = d;
        event.initialAmount = initial;
        event.allowMint = "true".equals(allowMintText);
        event.books = new ArrayList<>();
        event.holdings = new ArrayList<>();
        event.trades = new ArrayList<>();
        return event;
    }

    private static String getParsingErrorMessage(JAXBException exception) {
        Throwable cause = exception.getLinkedException();
        String message = cause != null ? cause.getMessage() : exception.getMessage();

        return message != null ? message : exception.toString();
    }
}
