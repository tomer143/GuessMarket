package JavaFX.Views.Dialogs;

import Engine.External.EventDetails;
import Engine.External.GuessMarketException;
import Engine.External.TradingMethod;
import Engine.GuessMarketEngine;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.io.IOException;
import java.io.UncheckedIOException;

public class CloseEventDialog {
    public static void show(GuessMarketEngine engine, EventDetails event, Runnable onSuccess) {
        FXMLLoader loader = new FXMLLoader(CloseEventDialog.class.getResource("CloseEventDialog.fxml"));

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Close Event");
        dialog.setHeaderText("Close \"" + event.name() + "\" and declare the winning option");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        try {
            dialog.getDialogPane().setContent(loader.load());
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }

        CloseEventDialogController controller = loader.getController();
        controller.init(event.mmUsername(), event.optionNames());

        dialog.showAndWait().filter(button -> button == ButtonType.OK).ifPresent(button -> {
            String username = controller.usernameProperty().get().trim();
            int winningIndex = controller.winningOptionIndexProperty().get();
            try {
                if (event.method() == TradingMethod.LMSR)
                    engine.closeEvent(event.id(), winningIndex, username);
                else
                    engine.closeOrderBookEvent(event.id(), winningIndex, username);

                AlertUtils.showInfo("Event closed", "\"" + event.name() + "\" has been closed.");
                onSuccess.run();
            } catch (GuessMarketException exception) {
                AlertUtils.showError("Could not close the event", exception.getMessage());
            }
        });
    }
}
