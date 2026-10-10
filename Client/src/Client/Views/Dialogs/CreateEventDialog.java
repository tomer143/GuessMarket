package Client.Views.Dialogs;

import Models.External.*;
import Client.Tasks.Background;
import Engine.ClientGuessMarketEngine;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.io.IOException;
import java.io.UncheckedIOException;

public class CreateEventDialog {
    public static void show(ClientGuessMarketEngine engine, String creatorUsername, Runnable onSuccess) {
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
            String name = controller.eventNameProperty().get().trim();
            String description = controller.descriptionProperty().get().trim();
            int feePercent = controller.feePercentProperty().get();
            FeeCollection feeCollection = controller.feeCollectionProperty().get();
            String optionAName = controller.optionANameProperty().get().trim();
            String optionBName = controller.optionBNameProperty().get().trim();
            TradingMethod method = controller.methodProperty().get();
            int liquidity = controller.liquidityProperty().get();
            int baseValue = controller.baseValueProperty().get();
            int initialAmount = controller.initialAmountProperty().get();
            boolean allowMint = controller.allowMintProperty().get();

            Background.fetch(() -> {
                if (method == TradingMethod.LMSR)
                    return engine.createLmsrEvent(name, description, feePercent, feeCollection,
                            optionAName, optionBName, liquidity, creatorUsername);
                return engine.createOrderBookEvent(name, description, feePercent, feeCollection,
                        optionAName, optionBName, baseValue, initialAmount, allowMint, creatorUsername);
            }, eventId -> {
                AlertUtils.showInfo("Event created", "\"" + name + "\" was created (id " + eventId + "). You are now its market maker.");
                onSuccess.run();
            }, exception -> AlertUtils.showError("Could not create the event", exception.getMessage()));
        });
    }
}
