package Client.Views.Dialogs;

import Models.External.*;
import Client.Tasks.Background;
import Engine.ClientGuessMarketEngine;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.io.IOException;
import java.io.UncheckedIOException;

public class OpenEventDialog {
    public static void show(ClientGuessMarketEngine engine, EventDetails event, Runnable onSuccess) {
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
            String username = controller.usernameProperty().get().trim();

            Background.fetch(() -> {
                engine.openEvent(event.id(), username);
                return null;
            }, result -> {
                AlertUtils.showInfo("Event opened", "\"" + event.name() + "\" is now active.");
                onSuccess.run();
            }, exception -> AlertUtils.showError("Could not open the event", exception.getMessage()));
        });
    }
}
