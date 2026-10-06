package Client.Views;

import Engine.ClientGuessMarketEngine;
import Client.Animations;
import Client.Tasks.Background;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.util.Duration;

import java.util.Timer;
import java.util.TimerTask;

public class RootLayoutController {
    private static final long POLL_INTERVAL_MILLIS = 800;

    @FXML private MFXToggleButton animationsToggle;
    @FXML private Label currentUserLabel;
    @FXML private MFXButton logoutButton;
    @FXML private TabPane tabPane;
    @FXML private Tab eventsTab;
    @FXML private Tab usersTab;

    @FXML private EventsViewController eventsViewController;
    @FXML private UsersViewController usersViewController;

    private ClientGuessMarketEngine engine;
    private Runnable onLogout;
    private Timer pollTimer;

    @FXML
    private void initialize() {
        animationsToggle.selectedProperty().bindBidirectional(Animations.enabledProperty());
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == eventsTab) eventsViewController.refresh();
            else if (newTab == usersTab) usersViewController.refresh();
            if (newTab != null) Animations.fadeIn(newTab.getContent(), Duration.millis(200));
        });
    }

    public void init(ClientGuessMarketEngine engine, String username, Runnable onLogout) {
        this.engine = engine;
        this.onLogout = onLogout;

        currentUserLabel.setText("Logged in as: " + username);

        eventsViewController.init(engine, username, this::refreshAll);
        usersViewController.init(engine, username, this::refreshAll);
        refreshAll();
        startPolling();
    }

    private void refreshAll() {
        eventsViewController.refresh();
        usersViewController.refresh();
    }

    private void startPolling() {
        pollTimer = new Timer("poll-timer", true);
        pollTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (Background.isIdle())
                    Platform.runLater(RootLayoutController.this::refreshAll);
            }
        }, POLL_INTERVAL_MILLIS, POLL_INTERVAL_MILLIS);
    }

    @FXML
    private void onLogoutClicked() {
        if (pollTimer != null) pollTimer.cancel();
        engine.logout();
        onLogout.run();
    }
}
