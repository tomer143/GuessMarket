package JavaFX.Models;

import Engine.External.UserSummary;
import javafx.beans.property.*;

public class UserRow {
    private final StringProperty username = new SimpleStringProperty();
    private final DoubleProperty balance = new SimpleDoubleProperty();
    private final BooleanProperty blocked = new SimpleBooleanProperty();

    public UserRow(UserSummary source) {
        update(source);
    }

    public void update(UserSummary source) {
        username.set(source.username());
        balance.set(source.balance());
        blocked.set(source.blocked());
    }

    public StringProperty usernameProperty() { return username; }
    public DoubleProperty balanceProperty() { return balance; }
    public BooleanProperty blockedProperty() { return blocked; }

    public String username() { return username.get(); }
}
