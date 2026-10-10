package Client.Views.Dialogs;

import Models.External.*;
import Client.Tasks.Background;
import Engine.ClientGuessMarketEngine;
import Client.Format;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.io.IOException;
import java.io.UncheckedIOException;

public class BuySharesDialog {
    public static void show(ClientGuessMarketEngine engine, EventDetails event, String username, Runnable onSuccess) {
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

        DialogStyling.applyTheme(dialog.getDialogPane());

        BuySharesDialogController controller = loader.getController();
        controller.init(event.optionNames());

        dialog.showAndWait().filter(button -> button == ButtonType.OK).ifPresent(button -> {
            int optionIndex = controller.optionIndexProperty().get();
            int amount = controller.amountProperty().get();

            Background.fetch(() -> engine.buyShares(event.id(), optionIndex, amount, username), result -> {
                AlertUtils.showInfo("Purchase successful",
                        "Shares cost: " + Format.decimal(result.sharesCost()) +
                        "\nFee: " + Format.decimal(result.feeAmount()) +
                        "\nTotal paid: " + Format.decimal(result.totalPaid()));
                onSuccess.run();
            }, exception -> AlertUtils.showError("Could not complete the purchase", exception.getMessage()));
        });
    }
}
