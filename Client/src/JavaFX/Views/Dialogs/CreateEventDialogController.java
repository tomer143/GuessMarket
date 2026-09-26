package JavaFX.Views.Dialogs;

import Engine.External.FeeCollection;
import Engine.External.TradingMethod;
import JavaFX.Format;
import JavaFX.Views.Components.ToggleChoiceBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class CreateEventDialogController {
    @FXML private MFXTextField usernameField;
    @FXML private MFXTextField nameField;
    @FXML private MFXTextField descriptionField;
    @FXML private Spinner<Integer> feeSpinner;
    @FXML private ToggleChoiceBox<FeeCollection> feeCollectionChoice;
    @FXML private MFXTextField optionAField;
    @FXML private MFXTextField optionBField;
    @FXML private ToggleChoiceBox<TradingMethod> methodChoice;
    @FXML private HBox lmsrFieldsBox;
    @FXML private Spinner<Integer> liquiditySpinner;
    @FXML private VBox orderBookFieldsBox;
    @FXML private Spinner<Integer> baseValueSpinner;
    @FXML private Spinner<Integer> initialAmountSpinner;
    @FXML private MFXToggleButton allowMintToggle;

    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty eventName = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final IntegerProperty feePercent = new SimpleIntegerProperty();
    private final ObjectProperty<FeeCollection> feeCollection = new SimpleObjectProperty<>();
    private final StringProperty optionAName = new SimpleStringProperty();
    private final StringProperty optionBName = new SimpleStringProperty();
    private final ObjectProperty<TradingMethod> method = new SimpleObjectProperty<>();
    private final IntegerProperty liquidity = new SimpleIntegerProperty();
    private final IntegerProperty baseValue = new SimpleIntegerProperty();
    private final IntegerProperty initialAmount = new SimpleIntegerProperty();
    private final BooleanProperty allowMint = new SimpleBooleanProperty();

    @FXML
    private void initialize() {
        feeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 90, 5));
        liquiditySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1_000_000, 100));
        baseValueSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1_000_000, 1));
        initialAmountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1_000_000, 0));

        feeCollectionChoice.setItems(List.of(FeeCollection.OnPurchase, FeeCollection.OnClose), Format::feeCollection);
        feeCollectionChoice.selectFirst();

        methodChoice.setItems(List.of(TradingMethod.LMSR, TradingMethod.ORDER_BOOK), Format::method);
        methodChoice.selectedItemProperty().addListener((observable, oldValue, newValue) -> updateMethodFieldsVisibility(newValue));
        methodChoice.selectFirst();
        updateMethodFieldsVisibility(methodChoice.selectedItemProperty().get());

        username.bind(usernameField.textProperty());
        eventName.bind(nameField.textProperty());
        description.bind(descriptionField.textProperty());
        feePercent.bind(feeSpinner.valueProperty());
        feeCollection.bind(feeCollectionChoice.selectedItemProperty());
        optionAName.bind(optionAField.textProperty());
        optionBName.bind(optionBField.textProperty());
        method.bind(methodChoice.selectedItemProperty());
        liquidity.bind(liquiditySpinner.valueProperty());
        baseValue.bind(baseValueSpinner.valueProperty());
        initialAmount.bind(initialAmountSpinner.valueProperty());
        allowMint.bind(allowMintToggle.selectedProperty());
    }

    private void updateMethodFieldsVisibility(TradingMethod selected) {
        boolean isLmsr = selected == TradingMethod.LMSR;
        lmsrFieldsBox.setVisible(isLmsr);
        lmsrFieldsBox.setManaged(isLmsr);
        orderBookFieldsBox.setVisible(!isLmsr);
        orderBookFieldsBox.setManaged(!isLmsr);
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public StringProperty eventNameProperty() {
        return eventName;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public IntegerProperty feePercentProperty() {
        return feePercent;
    }

    public ObjectProperty<FeeCollection> feeCollectionProperty() {
        return feeCollection;
    }

    public StringProperty optionANameProperty() {
        return optionAName;
    }

    public StringProperty optionBNameProperty() {
        return optionBName;
    }

    public ObjectProperty<TradingMethod> methodProperty() {
        return method;
    }

    public IntegerProperty liquidityProperty() {
        return liquidity;
    }

    public IntegerProperty baseValueProperty() {
        return baseValue;
    }

    public IntegerProperty initialAmountProperty() {
        return initialAmount;
    }

    public BooleanProperty allowMintProperty() {
        return allowMint;
    }
}
