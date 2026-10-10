package Client.Views.Dialogs;

import Models.External.*;
import Client.Tasks.Background;
import Engine.ClientGuessMarketEngine;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;

public class OpenEventDialog {
    public static void show(ClientGuessMarketEngine engine, EventDetails event, String username, Runnable onSuccess) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Open Event");
        dialog.setHeaderText("Open \"" + event.name() + "\" as its market maker?");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Label message = new Label("Opening makes the event active for trading. The opening cost (LMSR subsidy or "
                + "initial share purchase) is charged to your account.");
        message.setWrapText(true);
        message.setMaxWidth(380);
        message.setPadding(new Insets(12));
        dialog.getDialogPane().setContent(message);

        DialogStyling.applyTheme(dialog.getDialogPane());

        dialog.showAndWait().filter(button -> button == ButtonType.OK).ifPresent(button -> {
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
