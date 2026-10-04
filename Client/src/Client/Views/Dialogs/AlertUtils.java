package Client.Views.Dialogs;

import javafx.scene.control.Alert;

import java.util.HashSet;
import java.util.Set;

public class AlertUtils {
    private static final Set<String> reportedErrors = new HashSet<>();

    public static void showErrorOnce(String header, String message) {
        if (!reportedErrors.add(header + "\n" + message)) return;
        showError(header, message);
    }

    public static void clearReportedError(String header) {
        reportedErrors.removeIf(key -> key.startsWith(header + "\n"));
    }

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
