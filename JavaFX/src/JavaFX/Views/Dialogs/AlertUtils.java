package JavaFX.Views.Dialogs;

import javafx.scene.control.Alert;

public class AlertUtils {
    public static void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfo(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Guess Market");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
