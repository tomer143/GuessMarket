package JavaFX.Models;

import Engine.External.EventDetails;
import Engine.External.EventPhase;
import Engine.External.FeeCollection;
import Engine.External.TradingMethod;
import javafx.beans.property.*;

public class EventRow {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final ObjectProperty<EventPhase> phase = new SimpleObjectProperty<>();
    private final ObjectProperty<TradingMethod> method = new SimpleObjectProperty<>();
    private final IntegerProperty feePercent = new SimpleIntegerProperty();
    private final ObjectProperty<FeeCollection> feeCollection = new SimpleObjectProperty<>();
    private final StringProperty mmUsername = new SimpleStringProperty();
    private final DoubleProperty accountBalance = new SimpleDoubleProperty();

    public EventRow(EventDetails source) {
        update(source);
    }

    public void update(EventDetails source) {
        id.set(source.id());
        name.set(source.name());
        phase.set(source.phase());
        method.set(source.method());
        feePercent.set(source.feePercent());
        feeCollection.set(source.feeCollection());
        mmUsername.set(source.mmUsername());
        accountBalance.set(source.accountBalance());
    }

    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public ObjectProperty<EventPhase> phaseProperty() { return phase; }
    public ObjectProperty<TradingMethod> methodProperty() { return method; }
    public IntegerProperty feePercentProperty() { return feePercent; }
    public ObjectProperty<FeeCollection> feeCollectionProperty() { return feeCollection; }
    public StringProperty mmUsernameProperty() { return mmUsername; }
    public DoubleProperty accountBalanceProperty() { return accountBalance; }

    public int id() { return id.get(); }
    public EventPhase phase() { return phase.get(); }
    public TradingMethod method() { return method.get(); }
    public FeeCollection feeCollection() { return feeCollection.get(); }
}
