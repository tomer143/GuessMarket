package Client.Models;

import Models.External.BalanceLedgerEntry;
import javafx.beans.property.*;

public class LedgerRow {
    private final StringProperty description = new SimpleStringProperty();
    private final DoubleProperty amount = new SimpleDoubleProperty();
    private final DoubleProperty resultingBalance = new SimpleDoubleProperty();

    public LedgerRow(BalanceLedgerEntry source) {
        description.set(source.description());
        amount.set(source.amount());
        resultingBalance.set(source.resultingBalance());
    }

    public StringProperty descriptionProperty() { return description; }
    public DoubleProperty amountProperty() { return amount; }
    public DoubleProperty resultingBalanceProperty() { return resultingBalance; }
}
