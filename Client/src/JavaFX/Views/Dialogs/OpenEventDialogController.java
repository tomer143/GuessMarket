package JavaFX.Views.Dialogs;

import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;

public class OpenEventDialogController {
    @FXML private MFXTextField usernameField;

    private final StringProperty username = new SimpleStringProperty();

    @FXML
    private void initialize() {
        username.bind(usernameField.textProperty());
    }

    public void setDefaultUsername(String defaultUsername) {
        usernameField.setText(defaultUsername);
    }

    public StringProperty usernameProperty() {
        return username;
    }
}
