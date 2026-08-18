package Engine;

import Engine.External.FeeCollection;
import Engine.External.GuessMarketException;
import Engine.Xml.Comision;
import Engine.Xml.GMEvent;
import Engine.Xml.GuessMarket;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class EventFileLoader {
    private static final String GENERATED_PACKAGE_NAME = "Engine.Xml";

    public static void load(String path) throws GuessMarketException {
        validatePath(path);

        GuessMarket guessMarket = parseDocument(path);
        if (guessMarket.getGMEvents() == null)
            throw new GuessMarketException("The file does not contain a GM-events section.");

        List<GMEvent> eventElements = guessMarket.getGMEvents().getGMEvent();
        if (eventElements.isEmpty())
            throw new GuessMarketException("The file does not contain any events.");

        List<LmsrEvent> events = new ArrayList<>();
        Set<Integer> existIds = new HashSet<>();

        for (GMEvent eventElement : eventElements) {
            LmsrEvent event = parseEvent(eventElement);

            if (!existIds.add(event.id))
                throw new GuessMarketException("Duplicate event id " + event.id + " found in the file.");

            events.add(event);
        }

        MoneyMaker moneyMaker = new MoneyMaker(1, 0);

        for (LmsrEvent event : events) {
            double creationCost = event.getCreationCost();
            event.accountBalance = creationCost;
            moneyMaker.adjustBalance(-creationCost);
        }

        Manager.getInstance().replaceState(new ArrayList<>(events), moneyMaker);
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
        }
    }

    private static LmsrEvent parseEvent(GMEvent eventElement) throws GuessMarketException {
        try {
            LmsrEvent event = new LmsrEvent();
            event.name = eventElement.getName().trim();

            int id = eventElement.getId();
            if (id == 0)
                throw new GuessMarketException("Event \"" + event.name + "\" is missing a valid numeric id.");
            event.id = id;

            event.description = eventElement.getDescription().trim();

            Comision comision = eventElement.getComision();

            if (comision == null)
                throw new GuessMarketException("Event \"" + event.name + "\" is missing a valid comision value.");

            int feePercent = comision.getValue();
            if (feePercent < 0 || feePercent > 90)
                throw new GuessMarketException("Event \"" + event.name + "\" has an invalid fee of " + feePercent + "% (must be between 0 and 90).");
            event.feePercent = feePercent;

            if (comision.getType() == null)
                throw new GuessMarketException("Event \"" + event.name + "\" is missing a valid comision type.");
            String feeType = comision.getType().trim();
            if (feeType.equalsIgnoreCase("on-close"))
                event.feeCollection = FeeCollection.OnClose;
            else if (feeType.equalsIgnoreCase("on-purchase"))
                event.feeCollection = FeeCollection.OnPurchase;
            else
                throw new GuessMarketException("Event \"" + event.name + "\" has an unknown fee collection type \"" + feeType + "\".");

            List<String> optionNames = eventElement.getGMOptions().getGMOption();
            if (optionNames.size() != 2)
                throw new GuessMarketException("Event \"" + event.name + "\" must have exactly 2 options (found " + optionNames.size() + ").");

            List<Option> options = new ArrayList<>();
            for (int i = 0; i < optionNames.size(); i++) {
                options.add(new Option(i + 1, optionNames.get(i).trim()));
            }
            event.options = options;

            event.instability = eventElement.getGMMethod().getGMLMSR().getB();

            event.isActive = true;
            event.mmId = 1;

            return event;
        } catch (GuessMarketException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new GuessMarketException("The file contains an event with missing or malformed data: " + exception.getMessage());
        }
    }

    private static String getParsingErrorMessage(JAXBException exception) {
        Throwable cause = exception.getLinkedException();
        String message = cause != null ? cause.getMessage() : exception.getMessage();

        return message != null ? message : exception.toString();
    }
}
