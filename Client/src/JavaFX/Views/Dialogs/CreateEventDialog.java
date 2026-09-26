package JavaFX.Views.Dialogs;

import Engine.External.GuessMarketException;
import Engine.External.TradingMethod;
import Engine.GuessMarketEngine;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.io.IOException;
import java.io.UncheckedIOException;

public class CreateEventDialog {
    public static void show(GuessMarketEngine engine, Runnable onSuccess) {
        FXMLLoader loader = new FXMLLoader(CreateEventDialog.class.getResource("CreateEventDialog.fxml"));

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Event");
        dialog.setHeaderText("Create a new event and become its market maker");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        try {
            dialog.getDialogPane().setContent(loader.load());
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }

        dialog.getDialogPane().setPrefSize(400, 420);

        DialogStyling.applyTheme(dialog.getDialogPane());

        CreateEventDialogController controller = loader.getController();

        dialog.showAndWait().filter(button -> button == ButtonType.OK).ifPresent(button -> {
            try {
                String creatorUsername = controller.usernameProperty().get().trim();
                String name = controller.eventNameProperty().get().trim();
                String description = controller.descriptionProperty().get().trim();
                int feePercent = controller.feePercentProperty().get();
                String optionAName = controller.optionANameProperty().get().trim();
                String optionBName = controller.optionBNameProperty().get().trim();

                int eventId;
                if (controller.methodProperty().get() == TradingMethod.LMSR) {
                    eventId = engine.createLmsrEvent(name, description, feePercent, controller.feeCollectionProperty().get(),
                            optionAName, optionBName, controller.liquidityProperty().get(), creatorUsername);
                } else {
                    eventId = engine.createOrderBookEvent(name, description, feePercent, controller.feeCollectionProperty().get(),
                            optionAName, optionBName, controller.baseValueProperty().get(), controller.initialAmountProperty().get(),
                            controller.allowMintProperty().get(), creatorUsername);
                }

                AlertUtils.showInfo("Event created", "\"" + name + "\" was created (id " + eventId + "). You are now its market maker.");
                onSuccess.run();
            } catch (GuessMarketException exception) {
                AlertUtils.showError("Could not create the event", exception.getMessage());
            }
        });
    }
}
