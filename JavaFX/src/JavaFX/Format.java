package JavaFX;

import Engine.External.EventPhase;
import Engine.External.FeeCollection;
import Engine.External.TradingMethod;

import java.util.Locale;

public class Format {
    public static String decimal(double value) {
        return String.format("%.2f", value);
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
}
