package JavaFX.Views.Dialogs;

import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import java.util.List;

public class CloseEventDialogController {
    @FXML private MFXTextField usernameField;
    @FXML private MFXComboBox<String> optionChoice;

    private final StringProperty username = new SimpleStringProperty();
    private final IntegerProperty winningOptionIndex = new SimpleIntegerProperty();

    @FXML
    private void initialize() {
        username.bind(usernameField.textProperty());
        winningOptionIndex.bind(optionChoice.getSelectionModel().selectedIndexProperty());

        optionChoice.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> optionChoice.show());
    }

    public void init(String defaultUsername, List<String> optionNames) {
        usernameField.setText(defaultUsername);
        optionChoice.setItems(FXCollections.observableArrayList(optionNames));
        optionChoice.getSelectionModel().selectFirst();
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public IntegerProperty winningOptionIndexProperty() {
        return winningOptionIndex;
    }
}
