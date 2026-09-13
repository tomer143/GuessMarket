package JavaFX.Views.Components;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ToggleChoiceBox<T> extends HBox {
    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final List<T> items = new ArrayList<>();
    private final ReadOnlyIntegerWrapper selectedIndex = new ReadOnlyIntegerWrapper(this, "selectedIndex", -1);
    private final ReadOnlyObjectWrapper<T> selectedItem = new ReadOnlyObjectWrapper<>(this, "selectedItem");

    public ToggleChoiceBox() {
        super(6);
        toggleGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            int index = newToggle == null ? -1 : (int) newToggle.getUserData();
            selectedIndex.set(index);
            selectedItem.set(index < 0 ? null : items.get(index));
        });
    }

    public void setItems(List<T> newItems, Function<T, String> labelFor) {
        getChildren().clear();
        items.clear();
        items.addAll(newItems);

        for (int i = 0; i < items.size(); i++) {
            RadioButton radioButton = new RadioButton(labelFor.apply(items.get(i)));
            radioButton.setUserData(i);
            radioButton.setToggleGroup(toggleGroup);
            getChildren().add(radioButton);
        }
    }

    public void selectFirst() {
        if (!items.isEmpty()) ((RadioButton) getChildren().get(0)).setSelected(true);
    }

    public ReadOnlyIntegerProperty selectedIndexProperty() {
        return selectedIndex.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<T> selectedItemProperty() {
        return selectedItem.getReadOnlyProperty();
    }
}
