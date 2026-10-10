package Client.Views.Dialogs;

import Client.Views.Components.ToggleChoiceBox;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import java.util.List;

public class BuySharesDialogController {
    @FXML private ToggleChoiceBox<String> optionChoice;
    @FXML private Spinner<Integer> amountSpinner;

    private final IntegerProperty optionIndex = new SimpleIntegerProperty();
    private final IntegerProperty amount = new SimpleIntegerProperty();

    @FXML
    private void initialize() {
        amountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1_000_000, 1));

        optionIndex.bind(optionChoice.selectedIndexProperty());
        amount.bind(amountSpinner.valueProperty());
    }

    public void init(List<String> optionNames) {
        optionChoice.setItems(optionNames, name -> name);
        optionChoice.selectFirst();
    }

    public IntegerProperty optionIndexProperty() {
        return optionIndex;
    }

    public IntegerProperty amountProperty() {
        return amount;
    }
}
