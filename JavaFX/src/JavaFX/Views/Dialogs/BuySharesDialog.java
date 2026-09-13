package JavaFX.Views.Dialogs;

import Engine.External.EventDetails;
import Engine.External.GuessMarketException;
import Engine.External.PurchaseResult;
import Engine.GuessMarketEngine;
import JavaFX.Format;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.io.IOException;
import java.io.UncheckedIOException;

public class BuySharesDialog {
    public static void show(GuessMarketEngine engine, EventDetails event, String defaultUsername, Runnable onSuccess) {
        FXMLLoader loader = new FXMLLoader(BuySharesDialog.class.getResource("BuySharesDialog.fxml"));

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Buy Shares");
        dialog.setHeaderText("Buy shares of \"" + event.name() + "\"");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        try {
            dialog.getDialogPane().setContent(loader.load());
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }

        BuySharesDialogController controller = loader.getController();
        controller.init(defaultUsername == null ? "" : defaultUsername, event.optionNames());

        dialog.showAndWait().filter(button -> button == ButtonType.OK).ifPresent(button -> {
            try {
                PurchaseResult result = engine.buyShares(event.id(), controller.optionIndexProperty().get(),
                        controller.amountProperty().get(), controller.usernameProperty().get().trim());
                AlertUtils.showInfo("Purchase successful",
                        "Shares cost: " + Format.decimal(result.sharesCost()) +
                        "\nFee: " + Format.decimal(result.feeAmount()) +
                        "\nTotal paid: " + Format.decimal(result.totalPaid()));
                onSuccess.run();
            } catch (GuessMarketException exception) {
                AlertUtils.showError("Could not complete the purchase", exception.getMessage());
            }
        });
    }
}
