package JavaFX.Views.Components;

import io.github.palexdev.materialfx.controls.MFXToggleButton;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class FilterButtonGroup<O, V> extends HBox {
    private final List<MFXToggleButton> optionButtons = new ArrayList<>();
    private final List<O> optionValues = new ArrayList<>();
    private final MFXToggleButton allButton = new MFXToggleButton("All");
    private Runnable onChange = () -> {};
    private final Predicate<V> predicate;
    private boolean suppressChange = false;

    public FilterButtonGroup(String label, List<O> options, Function<O, String> labelFor, BiPredicate<O, V> matches) {
        super(6);
        getChildren().add(new Label(label + ":"));

        allButton.setSelected(true);
        allButton.selectedProperty().addListener((observable, wasSelected, isSelected) -> {
            if (suppressChange) return;
            suppressChange = true;
            for (MFXToggleButton button : optionButtons) button.setSelected(isSelected);
            suppressChange = false;
            fireChange();
        });
        getChildren().add(allButton);

        for (O option : options) {
            optionValues.add(option);
            MFXToggleButton button = new MFXToggleButton(labelFor.apply(option));
            button.setSelected(true);
            button.selectedProperty().addListener((observable, wasSelected, isSelected) -> {
                if (suppressChange) return;
                suppressChange = true;
                if (!isSelected) allButton.setSelected(false);
                else if (optionButtons.stream().allMatch(MFXToggleButton::isSelected)) allButton.setSelected(true);
                suppressChange = false;
                fireChange();
            });
            optionButtons.add(button);
            getChildren().add(button);
        }

        this.predicate = value -> {
            for (int i = 0; i < optionValues.size(); i++)
                if (optionButtons.get(i).isSelected() && matches.test(optionValues.get(i), value)) return true;
            return optionButtons.stream().noneMatch(MFXToggleButton::isSelected);
        };
    }

    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    private void fireChange() {
        onChange.run();
    }

    public Predicate<V> predicate() {
        return predicate;
    }
}
