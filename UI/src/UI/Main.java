package UI;

import Engine.GuessMarketEngine;
import Engine.External.*;

import java.util.List;

public class Main {
    private static final GuessMarketEngine engine = new GuessMarketEngine();
    private static final ConsoleUtils consoleUtils = new ConsoleUtils(System.in, System.out);

    public static void main() {
        boolean running = true;

        while (running) {
            printMenu();
            String choice = consoleUtils.readLine("Choose a menu option (number from 1 to 6): ");

            switch (choice) {
                case "1" -> loadEventsFile();
                case "2" -> displayEvents();
                case "3" -> showEventStatus();
                case "4" -> participateInEvent();
                case "5" -> closeEvent();
                case "6" -> running = false;
                default -> consoleUtils.println("Unknown option: \"" + choice + "\". Please choose a number from 1 to 6.");
            }
        }

        consoleUtils.println("Goodbye :)");
    }

    private static void printMenu() {
        consoleUtils.println("===== Guess Market =====");
        consoleUtils.println("1. Load events file");
        consoleUtils.println("2. Display events");
        consoleUtils.println("3. Show event trading status");
        consoleUtils.println("4. Participate in an event");
        consoleUtils.println("5. Close an event");
        consoleUtils.println("6. Exit");
    }

    private static void loadEventsFile() {
        String path = consoleUtils.readLine("Enter the full path to the events XML file: ");

        try {
            engine.loadEventsFile(path);
            consoleUtils.println("The file was loaded successfully.");
        } catch (GuessMarketException exception) {
            consoleUtils.println("Could not load the file: " + exception.getMessage());
        }
    }

    private static void displayEvents() {
        List<EventDetails> events = engine.getAllEvents();

        if (events.isEmpty()) {
            consoleUtils.println("There are no events loaded. Please load an events file first.");
            return;
        }

        for (int i = 0; i < events.size(); i++) {
            consoleUtils.println();
            printEventDetails(i + 1, events.get(i));
        }
    }

    private static void printEventDetails(int number, EventDetails event) {
        consoleUtils.println(number + ". " + event.name() + " (id " + event.id() + ")");
        consoleUtils.println("   Description: " + event.description());
        consoleUtils.println("   Fee: " + event.feePercent() + "% (" + feeCollectionLabel(event.feeCollection()) + ")");
        consoleUtils.println("   Options: " + String.join(", ", event.optionNames()));
        consoleUtils.println("   Status: " + (event.isActive() ? "Active" : "Closed"));
    }

    private static String feeCollectionLabel(FeeCollection feeCollection) {
        return feeCollection == FeeCollection.OnPurchase ? "on purchase" : "on close";
    }

    private static void showEventStatus() {
        List<EventDetails> events = engine.getAllEvents();

        if (events.isEmpty()) {
            consoleUtils.println("There are no events loaded. Please load an events file first.");
            return;
        }

        EventDetails selected = pickEvent(events);
        if (selected == null) return;

        try {
            printEventStatus(engine.getEventStatus(selected.id()));
        } catch (GuessMarketException exception) {
            consoleUtils.println("Could not show the event status: " + exception.getMessage());
        }
    }

    private static void participateInEvent() {
        List<EventDetails> activeEvents = engine.getActiveEvents();

        if (activeEvents.isEmpty()) {
            consoleUtils.println("There are no active events to participate in.");
            return;
        }

        EventDetails selected = pickEvent(activeEvents);
        if (selected == null) return;

        try {
            printEventStatus(engine.getEventStatus(selected.id()));
        } catch (GuessMarketException exception) {
            consoleUtils.println("Could not show the event status: " + exception.getMessage());
            return;
        }

        Integer optionIndex = consoleUtils.pickFromList(selected.optionNames(), "Choose an option by number: ", "option");
        if (optionIndex == null) return;

        Integer amount = consoleUtils.readInt("How many shares would you like to buy? ");
        if (amount == null) return;

        try {
            PurchaseResult result = engine.buyShares(selected.id(), optionIndex, amount);
            consoleUtils.println();
            consoleUtils.println("Purchase successful.");
            consoleUtils.println("   Shares cost: " + ConsoleUtils.formatDecimal(result.sharesCost()));
            if (result.feeAmount() > 0)
                consoleUtils.println("   Fee: " + ConsoleUtils.formatDecimal(result.feeAmount()));
            consoleUtils.println("   Total paid: " + ConsoleUtils.formatDecimal(result.totalPaid()));

            printEventStatus(engine.getEventStatus(selected.id()));
        } catch (GuessMarketException exception) {
            consoleUtils.println("Could not complete the purchase: " + exception.getMessage());
        }
    }

    private static void closeEvent() {
        List<EventDetails> activeEvents = engine.getActiveEvents();

        if (activeEvents.isEmpty()) {
            consoleUtils.println("There are no active events to close.");
            return;
        }

        EventDetails selected = pickEvent(activeEvents);
        if (selected == null) return;

        try {
            printEventStatus(engine.getEventStatus(selected.id()));
        } catch (GuessMarketException exception) {
            consoleUtils.println("Could not show the event status: " + exception.getMessage());
            return;
        }

        Integer winningOptionIndex = consoleUtils.pickFromList(selected.optionNames(), "Choose an option by number: ", "option");
        if (winningOptionIndex == null) return;

        try {
            EventStatus finalStatus = engine.closeEvent(selected.id(), winningOptionIndex);
            consoleUtils.println();
            consoleUtils.println("The event has been closed.");
            printEventStatus(finalStatus);
        } catch (GuessMarketException exception) {
            consoleUtils.println("Could not close the event: " + exception.getMessage());
        }
    }

    private static EventDetails pickEvent(List<EventDetails> events) {
        List<String> labels = events.stream()
                .map(event -> event.name() + (event.isActive() ? "" : " (closed)"))
                .toList();

        Integer index = consoleUtils.pickFromList(labels, "Choose an event by number: ", "event");

        return index != null ? events.get(index) : null;
    }

    private static void printEventStatus(EventStatus status) {
        consoleUtils.println();
        consoleUtils.println("Status of \"" + status.eventName() + "\":");

        for (OptionStatus optionStatus : status.optionStatuses()) {
            consoleUtils.println("   " + optionStatus.name() + ": chance " + ConsoleUtils.formatDecimal(optionStatus.chance())
                    + ", total shares bought " + optionStatus.totalSharesBought());
        }

        consoleUtils.println("   Event account balance: " + ConsoleUtils.formatDecimal(status.accountBalance()));
        consoleUtils.println("   Total fee collected so far: " + ConsoleUtils.formatDecimal(status.totalFeeCollected()));

        consoleUtils.println("   Trade history (most recent first):");
        if (status.history().isEmpty()) {
            consoleUtils.println("      (no trades yet)");
        } else {
            for (TradeRecord trade : status.history()) {
                consoleUtils.println("      Bought " + trade.amount() + " share(s) of \"" + trade.optionName()
                        + "\" for " + ConsoleUtils.formatDecimal(trade.pricePaid()));
            }
        }

        if (!status.isActive()) {
            consoleUtils.println("   This event is closed. Winning option: " + status.winningOptionName());
        }
    }
}
