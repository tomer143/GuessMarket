package JavaFX.Views.Dialogs;

import Engine.External.EventDetails;
import Engine.External.GuessMarketException;
import Engine.GuessMarketEngine;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.io.IOException;
import java.io.UncheckedIOException;

public class OpenEventDialog {
    public static void show(GuessMarketEngine engine, EventDetails event, Runnable onSuccess) {
        FXMLLoader loader = new FXMLLoader(OpenEventDialog.class.getResource("OpenEventDialog.fxml"));

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Open Event");
        dialog.setHeaderText("Open \"" + event.name() + "\" as its market maker (" + event.mmUsername() + ")");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        try {
            dialog.getDialogPane().setContent(loader.load());
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }

        DialogStyling.applyTheme(dialog.getDialogPane());

        OpenEventDialogController controller = loader.getController();
        controller.setDefaultUsername(event.mmUsername());

        dialog.showAndWait().filter(button -> button == ButtonType.OK).ifPresent(button -> {
            try {
                engine.openEvent(event.id(), controller.usernameProperty().get().trim());
                AlertUtils.showInfo("Event opened", "\"" + event.name() + "\" is now active.");
                onSuccess.run();
            } catch (GuessMarketException exception) {
                AlertUtils.showError("Could not open the event", exception.getMessage());
            }
        });
    }
}
