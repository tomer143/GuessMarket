package JavaFX.Models;

import Engine.External.TradingMethod;
import Engine.External.UserEventParticipation;
import javafx.beans.property.*;

public class ParticipationRow {
    private final IntegerProperty eventId = new SimpleIntegerProperty();
    private final StringProperty eventName = new SimpleStringProperty();
    private final ObjectProperty<TradingMethod> method = new SimpleObjectProperty<>();
    private final BooleanProperty mm = new SimpleBooleanProperty();

    public ParticipationRow(UserEventParticipation source) {
        eventId.set(source.eventId());
        eventName.set(source.eventName());
        method.set(source.method());
        mm.set(source.isMm());
    }

    public IntegerProperty eventIdProperty() { return eventId; }
    public StringProperty eventNameProperty() { return eventName; }
    public ObjectProperty<TradingMethod> methodProperty() { return method; }
    public BooleanProperty mmProperty() { return mm; }

    public int eventId() { return eventId.get(); }
}
