package JavaFX.Views.Dialogs;

import JavaFX.Views.Components.ToggleChoiceBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import java.util.List;

public class BuySharesDialogController {
    @FXML private MFXTextField usernameField;
    @FXML private ToggleChoiceBox<String> optionChoice;
    @FXML private Spinner<Integer> amountSpinner;

    private final StringProperty username = new SimpleStringProperty();
    private final IntegerProperty optionIndex = new SimpleIntegerProperty();
    private final IntegerProperty amount = new SimpleIntegerProperty();

    @FXML
    private void initialize() {
        amountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1_000_000, 1));

        username.bind(usernameField.textProperty());
        optionIndex.bind(optionChoice.selectedIndexProperty());
        amount.bind(amountSpinner.valueProperty());
    }

    public void init(String defaultUsername, List<String> optionNames) {
        usernameField.setText(defaultUsername);
        optionChoice.setItems(optionNames, name -> name);
        optionChoice.selectFirst();
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public IntegerProperty optionIndexProperty() {
        return optionIndex;
    }

    public IntegerProperty amountProperty() {
        return amount;
    }
}
