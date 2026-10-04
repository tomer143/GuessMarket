package Engine;

import Engine.Xml.Commission;
import Engine.Xml.GMEvent;
import Engine.Xml.GMLMSR;
import Engine.Xml.GMMethod;
import Engine.Xml.GMOptions;
import Engine.Xml.GMOrderBook;
import Engine.Xml.GuessMarket;
import Models.External.FeeCollection;
import Models.External.GuessMarketException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class EventFileLoader {
    private static final String GENERATED_PACKAGE_NAME = "Engine.Xml";

    public static void loadFromFile(String path) throws GuessMarketException {
        validatePath(path);

        try (InputStream inputStream = new FileInputStream(path)) {
            loadFromStream(inputStream, null);
        } catch (IOException exception) {
            throw new GuessMarketException("The file \"" + path + "\" could not be read: " + exception.getMessage());
        }
    }

    public static void loadFromStream(InputStream inputStream, String uploaderUsername) throws GuessMarketException {
        GuessMarket guessMarket = parseDocument(inputStream);
        if (guessMarket == null)
            throw new GuessMarketException("The file could not be parsed as valid XML.");

        if (guessMarket.getGMEvents() == null)
            throw new GuessMarketException("The file does not contain a GM-events section.");

        List<GMEvent> eventElements = guessMarket.getGMEvents().getGMEvent();
        if (eventElements.isEmpty())
            throw new GuessMarketException("The file does not contain any events.");

        Set<String> existingEventNames = Manager.getInstance().getEvents().stream()
                .map(event -> event.name.toLowerCase())
                .collect(java.util.stream.Collectors.toSet());
        Set<String> namesInThisFile = new HashSet<>();

        List<Event> events = new ArrayList<>();
        int nextId = Manager.getInstance().getEvents().stream().mapToInt(event -> event.id).max().orElse(0);

        for (GMEvent eventElement : eventElements) {
            if (eventElement == null)
                throw new GuessMarketException("The file contains an empty event entry.");

            Event event = parseEvent(eventElement);

            String lowerName = event.name.toLowerCase();
            if (existingEventNames.contains(lowerName) || namesInThisFile.contains(lowerName))
                throw new GuessMarketException("An event named \"" + event.name + "\" already exists.");
            namesInThisFile.add(lowerName);

            nextId++;
            event.id = nextId;
            event.mmUsername = uploaderUsername;

            events.add(event);
        }

        for (Event event : events)
            Manager.getInstance().addEvent(event);
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

    private static GuessMarket parseDocument(InputStream inputStream) throws GuessMarketException {
        try {
            JAXBContext context = JAXBContext.newInstance(GENERATED_PACKAGE_NAME);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (GuessMarket) unmarshaller.unmarshal(inputStream);
        } catch (JAXBException exception) {
            throw new GuessMarketException("The file could not be parsed as valid XML: " + getParsingErrorMessage(exception));
        } catch (ClassCastException exception) {
            throw new GuessMarketException("The file's root element is not a Guess-Market document.");
        }
    }

    private static Event parseEvent(GMEvent eventElement) throws GuessMarketException {
        try {
            return parseEventCommon(eventElement);
        } catch (GuessMarketException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new GuessMarketException("The file contains an event with missing or malformed data: " + exception.getMessage());
        }
    }

    private static Event parseEventCommon(GMEvent eventElement) throws GuessMarketException {
        try {
            String name = eventElement.getName();
            if (name == null || name.isBlank())
                throw new GuessMarketException("The file contains an event with a missing or blank name.");
            String eventName = name.trim();

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

            event.name = eventName;
            event.description = description.trim();
            event.feePercent = feePercent;
            event.feeCollection = feeCollection;
            event.options = options;
            event.phase = Models.External.EventPhase.NOT_ACTIVE;

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
