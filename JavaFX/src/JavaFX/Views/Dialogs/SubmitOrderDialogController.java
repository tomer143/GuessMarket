package JavaFX.Views.Dialogs;

import Engine.External.OrderAction;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.MouseEvent;
import java.util.List;

public class SubmitOrderDialogController {
    @FXML private MFXTextField usernameField;
    @FXML private MFXComboBox<String> optionChoice;
    @FXML private MFXComboBox<OrderAction> sideChoice;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private MFXTextField priceField;

    private final StringProperty username = new SimpleStringProperty();
    private final IntegerProperty optionIndex = new SimpleIntegerProperty();
    private final ObjectProperty<OrderAction> side = new SimpleObjectProperty<>();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final StringProperty priceText = new SimpleStringProperty();

    @FXML
    private void initialize() {
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1_000_000, 1));

        sideChoice.setItems(FXCollections.observableArrayList(OrderAction.BUY, OrderAction.SELL));
        sideChoice.getSelectionModel().selectFirst();

        username.bind(usernameField.textProperty());
        optionIndex.bind(optionChoice.getSelectionModel().selectedIndexProperty());
        side.bind(sideChoice.getSelectionModel().selectedItemProperty());
        quantity.bind(quantitySpinner.valueProperty());
        priceText.bind(priceField.textProperty());

        optionChoice.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> optionChoice.show());
        sideChoice.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> sideChoice.show());
    }

    public void init(String defaultUsername, List<String> optionNames) {
        usernameField.setText(defaultUsername);
        optionChoice.setItems(FXCollections.observableArrayList(optionNames));
        optionChoice.getSelectionModel().selectFirst();
    }

    public StringProperty usernameProperty() {
        return username;
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
