package Client.Views.Dialogs;

import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;

public class DepositDialogController {
    @FXML private MFXTextField amountField;

    private final StringProperty amountText = new SimpleStringProperty();

    @FXML
    private void initialize() {
        amountText.bind(amountField.textProperty());
    }

    public StringProperty amountTextProperty() {
        return amountText;
    }
}
