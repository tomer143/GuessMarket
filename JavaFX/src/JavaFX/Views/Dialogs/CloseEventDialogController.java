package JavaFX.Views.Dialogs;

import JavaFX.Views.Components.ToggleChoiceBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import java.util.List;

public class CloseEventDialogController {
    @FXML private MFXTextField usernameField;
    @FXML private ToggleChoiceBox<String> optionChoice;

    private final StringProperty username = new SimpleStringProperty();
    private final IntegerProperty winningOptionIndex = new SimpleIntegerProperty();

    @FXML
    private void initialize() {
        username.bind(usernameField.textProperty());
        winningOptionIndex.bind(optionChoice.selectedIndexProperty());
    }

    public void init(String defaultUsername, List<String> optionNames) {
        usernameField.setText(defaultUsername);
        optionChoice.setItems(optionNames, name -> name);
        optionChoice.selectFirst();
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public IntegerProperty winningOptionIndexProperty() {
        return winningOptionIndex;
    }
}
