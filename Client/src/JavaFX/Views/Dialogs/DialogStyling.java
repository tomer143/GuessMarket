package JavaFX.Views.Dialogs;

import io.github.palexdev.materialfx.MFXResourcesLoader;
import javafx.scene.control.DialogPane;

class DialogStyling {
    static void applyTheme(DialogPane pane) {
        pane.getStylesheets().add(MFXResourcesLoader.load("css/DefaultTheme.css"));
        pane.getStylesheets().add(DialogStyling.class.getResource("/JavaFX/Views/app.css").toExternalForm());
    }
}
