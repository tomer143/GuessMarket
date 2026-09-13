package JavaFX.Views.Dialogs;

import Engine.External.EventDetails;
import Engine.External.GuessMarketException;
import Engine.External.OrderResult;
import Engine.GuessMarketEngine;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.io.IOException;
import java.io.UncheckedIOException;

public class SubmitOrderDialog {
    public static void show(GuessMarketEngine engine, EventDetails event, String defaultUsername, Runnable onSuccess) {
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

            try {
                OrderResult result = engine.submitOrder(event.id(), controller.optionIndexProperty().get(), controller.sideProperty().get(),
                        controller.quantityProperty().get(), price, controller.usernameProperty().get().trim());
                String summary = result.fills().isEmpty()
                        ? "No immediate match. " + result.unfilledQuantity() + " share(s) now resting in the order book."
                        : result.fills().size() + " fill(s), " + result.unfilledQuantity() + " share(s) still unfilled.";
                AlertUtils.showInfo("Order submitted", summary);
                onSuccess.run();
            } catch (GuessMarketException exception) {
                AlertUtils.showError("Could not submit the order", exception.getMessage());
            }
        });
    }
}
