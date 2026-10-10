package Client.Views.Dialogs;

import Models.External.OrderAction;
import Client.Format;
import Client.Views.Components.ToggleChoiceBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import java.util.List;

public class SubmitOrderDialogController {
    @FXML private ToggleChoiceBox<String> optionChoice;
    @FXML private ToggleChoiceBox<OrderAction> sideChoice;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private MFXTextField priceField;

    private final IntegerProperty optionIndex = new SimpleIntegerProperty();
    private final ObjectProperty<OrderAction> side = new SimpleObjectProperty<>();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final StringProperty priceText = new SimpleStringProperty();

    @FXML
    private void initialize() {
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1_000_000, 1));

        sideChoice.setItems(List.of(OrderAction.BUY, OrderAction.SELL), Format::orderAction);
        sideChoice.selectFirst();

        optionIndex.bind(optionChoice.selectedIndexProperty());
        side.bind(sideChoice.selectedItemProperty());
        quantity.bind(quantitySpinner.valueProperty());
        priceText.bind(priceField.textProperty());
    }

    public void init(List<String> optionNames) {
        optionChoice.setItems(optionNames, name -> name);
        optionChoice.selectFirst();
    }

    public IntegerProperty optionIndexProperty() {
        return optionIndex;
    }

    public ObjectProperty<OrderAction> sideProperty() {
        return side;
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public StringProperty priceTextProperty() {
        return priceText;
    }
}
