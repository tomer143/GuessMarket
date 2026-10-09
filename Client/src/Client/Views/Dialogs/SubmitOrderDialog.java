package Client.Views.Dialogs;

import Models.External.*;
import Client.Tasks.Background;
import Engine.ClientGuessMarketEngine;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.io.IOException;
import java.io.UncheckedIOException;

public class SubmitOrderDialog {
    public static void show(ClientGuessMarketEngine engine, EventDetails event, String defaultUsername, Runnable onSuccess) {
        FXMLLoader loader = new FXMLLoader(SubmitOrderDialog.class.getResource("SubmitOrderDialog.fxml"));

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Submit Order");
        dialog.setHeaderText("Submit an order for \"" + event.name() + "\"");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        try {
            dialog.getDialogPane().setContent(loader.load());
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }

        DialogStyling.applyTheme(dialog.getDialogPane());

        SubmitOrderDialogController controller = loader.getController();
        controller.init(defaultUsername == null ? "" : defaultUsername, event.optionNames());

        dialog.showAndWait().filter(button -> button == ButtonType.OK).ifPresent(button -> {
            double price;
            try {
                price = Double.parseDouble(controller.priceTextProperty().get().trim());
            } catch (NumberFormatException exception) {
                AlertUtils.showError("Could not submit the order", "\"" + controller.priceTextProperty().get() + "\" is not a valid price.");
                return;
            }

            int optionIndex = controller.optionIndexProperty().get();
            OrderAction side = controller.sideProperty().get();
            int quantity = controller.quantityProperty().get();
            String username = controller.usernameProperty().get().trim();

            Background.fetch(() -> engine.submitOrder(event.id(), optionIndex, side, quantity, price, username), result -> {
                String summary = result.fills().isEmpty()
                        ? "No immediate match. " + result.unfilledQuantity() + " share(s) now resting in the order book."
                        : result.fills().size() + " fill(s), " + result.unfilledQuantity() + " share(s) still unfilled.";
                AlertUtils.showInfo("Order submitted", summary);
                onSuccess.run();
            }, exception -> AlertUtils.showError("Could not submit the order", exception.getMessage()));
        });
    }
}
