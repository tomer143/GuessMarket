package Client.Views.Dialogs;

import Models.External.GuessMarketException;
import Engine.ClientGuessMarketEngine;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.io.IOException;
import java.io.UncheckedIOException;

public class DepositDialog {
    public static void show(ClientGuessMarketEngine engine, String username, Runnable onSuccess) {
        FXMLLoader loader = new FXMLLoader(DepositDialog.class.getResource("DepositDialog.fxml"));

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Deposit Funds");
        dialog.setHeaderText("Deposit funds into your account");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        try {
            dialog.getDialogPane().setContent(loader.load());
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }

        DialogStyling.applyTheme(dialog.getDialogPane());

        DepositDialogController controller = loader.getController();

        dialog.showAndWait().filter(button -> button == ButtonType.OK).ifPresent(button -> {
            double amount;
            try {
                amount = Double.parseDouble(controller.amountTextProperty().get().trim());
            } catch (NumberFormatException exception) {
                AlertUtils.showError("Could not deposit funds", "\"" + controller.amountTextProperty().get() + "\" is not a valid amount.");
                return;
            }

            try {
                engine.depositFunds(username, amount);
                AlertUtils.showInfo("Deposit successful", "Deposited " + amount + " into your account.");
                onSuccess.run();
            } catch (GuessMarketException exception) {
                AlertUtils.showError("Could not deposit funds", exception.getMessage());
            }
        });
    }
}
