package JavaFX;

import Engine.External.EventPhase;
import Engine.External.FeeCollection;
import Engine.External.OrderAction;
import Engine.External.TradingMethod;
import javafx.util.StringConverter;

import java.util.Locale;

public class Format {
    public static String decimal(double value) {
        return String.format("%.2f", value);
    }

    public static StringConverter<Number> integerAxisFormatter() {
        return new StringConverter<>() {
            @Override
            public String toString(Number value) {
                return value == null ? "" : String.valueOf(Math.round(value.doubleValue()));
            }

            @Override
            public Number fromString(String string) {
                return Long.parseLong(string);
            }
        };
    }

    public static String feeCollection(FeeCollection feeCollection) {
        return feeCollection == FeeCollection.OnPurchase ? "on purchase" : "on close";
    }

    public static String phase(EventPhase phase) {
        return switch (phase) {
            case NOT_ACTIVE -> "Not active";
            case ACTIVE -> "Active";
            case CLOSED -> "Closed";
        };
    }

    public static String method(TradingMethod method) {
        return method == TradingMethod.LMSR ? "LMSR" : "Order Book";
    }

    public static String orderAction(OrderAction side) {
        return side == OrderAction.BUY ? "Buy" : "Sell";
    }
}
