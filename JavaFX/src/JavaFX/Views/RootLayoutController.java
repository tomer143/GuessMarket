package JavaFX.Views;

import Engine.GuessMarketEngine;
import JavaFX.Tasks.LoadEventsFileTask;
import JavaFX.Views.Dialogs.AlertUtils;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXProgressBar;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class RootLayoutController {
    @FXML private MFXButton loadButton;
    @FXML private Label filePathLabel;
    @FXML private MFXProgressBar progressBar;
    @FXML private Label progressLabel;
    @FXML private Label placeholderLabel;
    @FXML private TabPane tabPane;
    @FXML private Tab eventsTab;
    @FXML private Tab usersTab;

    @FXML private EventsViewController eventsViewController;
    @FXML private UsersViewController usersViewController;

    private GuessMarketEngine engine;
    private Stage primaryStage;

    @FXML
    private void initialize() {
        showPlaceholder(true);
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == eventsTab) eventsViewController.refresh();
            else if (newTab == usersTab) usersViewController.refresh();
        });
    }

    public void init(GuessMarketEngine engine, Stage primaryStage) {
        this.engine = engine;
        this.primaryStage = primaryStage;
        eventsViewController.init(engine);
        usersViewController.init(engine);
    }

    @FXML
    private void onLoadClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files", "*.xml"));
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file == null) return;

        loadButton.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setManaged(true);

        LoadEventsFileTask task = new LoadEventsFileTask(engine, file.getAbsolutePath());
        progressBar.progressProperty().bind(task.progressProperty());
        progressLabel.textProperty().bind(task.messageProperty());

        task.setOnSucceeded(event -> {
            unbindProgress();
            loadButton.setDisable(false);
            filePathLabel.setText(file.getAbsolutePath());
            showPlaceholder(false);
            eventsViewController.refresh();
            usersViewController.refresh();
        });

        task.setOnFailed(event -> {
            unbindProgress();
            loadButton.setDisable(false);
            Throwable exception = task.getException();
            AlertUtils.showError("Could not load the file", exception == null ? "Unknown error." : exception.getMessage());
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void unbindProgress() {
        progressBar.progressProperty().unbind();
        progressLabel.textProperty().unbind();
        progressBar.setVisible(false);
        progressBar.setManaged(false);
    }

    private void showPlaceholder(boolean show) {
        placeholderLabel.setVisible(show);
        placeholderLabel.setManaged(show);
        tabPane.setVisible(!show);
        tabPane.setManaged(!show);
    }
}
