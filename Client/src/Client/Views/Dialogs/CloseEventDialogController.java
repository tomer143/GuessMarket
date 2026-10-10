package Client.Views.Dialogs;

import Client.Views.Components.ToggleChoiceBox;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import java.util.List;

public class CloseEventDialogController {
    @FXML private ToggleChoiceBox<String> optionChoice;

    private final IntegerProperty winningOptionIndex = new SimpleIntegerProperty();

    @FXML
    private void initialize() {
        winningOptionIndex.bind(optionChoice.selectedIndexProperty());
    }

    public void init(List<String> optionNames) {
        optionChoice.setItems(optionNames, name -> name);
        optionChoice.selectFirst();
    }

    public IntegerProperty winningOptionIndexProperty() {
        return winningOptionIndex;
    }
}
