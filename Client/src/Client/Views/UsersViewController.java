package Client.Views;

import Models.External.*;
import Engine.ClientGuessMarketEngine;
import Client.Animations;
import Client.Format;
import Client.Models.LedgerRow;
import Client.Models.ParticipationRow;
import Client.Models.UserRow;
import Client.Tasks.Background;
import Client.Tasks.LoadEventsFileTask;
import Client.Views.Dialogs.AlertUtils;
import Client.Views.Dialogs.DepositDialog;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXProgressBar;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UsersViewController {
    @FXML private MFXButton loadButton;
    @FXML private Label filePathLabel;
    @FXML private MFXProgressBar progressBar;
    @FXML private Label progressLabel;

    @FXML private TableView<UserRow> table;
    @FXML private TableColumn<UserRow, Number> balanceColumn;
    @FXML private TableColumn<UserRow, String> mmColumn;

    @FXML private VBox accountDetailsBox;
    @FXML private Label balanceLabel;
    @FXML private MFXButton depositButton;

    @FXML private TableView<ParticipationRow> participationsTable;
    @FXML private TableColumn<ParticipationRow, TradingMethod> methodColumn;
    @FXML private TableColumn<ParticipationRow, Boolean> roleColumn;
    @FXML private VBox eventDetailContainer;

    private final ObservableList<UserRow> rows = FXCollections.observableArrayList();
    private final ObservableList<ParticipationRow> participations = FXCollections.observableArrayList();
    private ClientGuessMarketEngine engine;
    private String currentUsername;
    private Runnable onDataChanged;
    private Set<String> mmUsernames = Set.of();
    private Integer selectedEventId;
    private EventDetailPane shownEventPane;
    private Integer shownEventId;
    private boolean updatingParticipations;
    private int shownLedgerSize = -1;

    @FXML
    private void initialize() {
        table.setItems(rows);
        balanceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : Format.decimal(item.doubleValue()));
            }
        });
        mmColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().username()));
        mmColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (mmUsernames.contains(item) ? "Yes" : "No"));
            }
        });

        participationsTable.setItems(participations);
        participationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        methodColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(TradingMethod item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : Format.method(item));
            }
        });
        roleColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (item ? "Market maker" : "Participant"));
            }
        });
        participationsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, participation) -> {
            if (updatingParticipations) return;
            selectedEventId = participation == null ? null : participation.eventId();
            showSelectedEvent();
        });

        depositButton.setOnAction(e -> DepositDialog.show(engine, currentUsername, onDataChanged));
    }

    public void init(ClientGuessMarketEngine engine, String currentUsername, Runnable onDataChanged) {
        this.engine = engine;
        this.currentUsername = currentUsername;
        this.onDataChanged = onDataChanged;
    }

    @FXML
    private void onLoadClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files", "*.xml"));
        File file = fileChooser.showOpenDialog(loadButton.getScene().getWindow());
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
            onDataChanged.run();
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

    private void showOwnAccount(Snapshot snapshot) {
        if (snapshot.accountError != null) {
            AlertUtils.showErrorOnce("Could not load user details", snapshot.accountError);
            return;
        }

        UserDetails details = snapshot.details;
        balanceLabel.setText("Account balance: " + Format.decimal(details.balance()) + (details.blocked() ? "   (BLOCKED)" : ""));

        List<BalanceLedgerEntry> ledger = snapshot.ledger;
        if (ledger.size() != shownLedgerSize) {
            boolean firstBuild = shownLedgerSize < 0;
            shownLedgerSize = ledger.size();
            VBox content = new VBox(10, buildBalanceChart(snapshot.balanceHistory), buildLedgerTable(ledger));
            accountDetailsBox.getChildren().setAll(content);
            if (firstBuild) Animations.fadeIn(content, Duration.millis(250));
        }

        updateParticipations(details.participations());
        AlertUtils.clearReportedError("Could not load user details");
    }

    private void updateParticipations(List<UserEventParticipation> latest) {
        boolean unchanged = latest.size() == participations.size();
        for (int i = 0; unchanged && i < latest.size(); i++)
            unchanged = latest.get(i).eventId() == participations.get(i).eventId();

        if (!unchanged) {
            updatingParticipations = true;
            try {
                participations.setAll(latest.stream().map(ParticipationRow::new).toList());
                ParticipationRow selected = participations.stream().filter(row -> row.eventId() == selectedEventIdOrNone())
                        .findFirst().orElse(null);
                if (selected != null) participationsTable.getSelectionModel().select(selected);
                else selectedEventId = null;
            } finally {
                updatingParticipations = false;
            }
        }
        showSelectedEvent();
    }

    private int selectedEventIdOrNone() {
        return selectedEventId == null ? Integer.MIN_VALUE : selectedEventId;
    }

    private void showSelectedEvent() {
        if (selectedEventId == null) {
            shownEventPane = null;
            shownEventId = null;
            eventDetailContainer.getChildren().setAll(new Label("Select an event above to see its details and trade."));
            return;
        }
        if (shownEventPane != null && selectedEventId.equals(shownEventId)) {
            shownEventPane.refresh();
            return;
        }
        shownEventId = selectedEventId;
        shownEventPane = new EventDetailPane(engine, selectedEventId, currentUsername, onDataChanged);
        VBox.setVgrow(shownEventPane, Priority.ALWAYS);
        eventDetailContainer.getChildren().setAll(shownEventPane);
    }

    private static TableView<LedgerRow> buildLedgerTable(List<BalanceLedgerEntry> ledger) {
        TableView<LedgerRow> table = new TableView<>();
        TableColumn<LedgerRow, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("description"));
        TableColumn<LedgerRow, Number> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("amount"));
        TableColumn<LedgerRow, Number> balanceColumn = new TableColumn<>("Balance");
        balanceColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("resultingBalance"));
        table.getColumns().addAll(descriptionColumn, amountColumn, balanceColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        List<LedgerRow> mostRecentFirst = ledger.stream().map(LedgerRow::new).collect(Collectors.toList());
        java.util.Collections.reverse(mostRecentFirst);
        table.setItems(FXCollections.observableArrayList(mostRecentFirst));

        double rowHeight = 28;
        int maxVisibleRows = 5;
        table.setFixedCellSize(rowHeight);
        table.setPrefHeight(rowHeight * (Math.min(Math.max(mostRecentFirst.size(), 1), maxVisibleRows) + 1) + 2);
        table.setMinHeight(Region.USE_PREF_SIZE);
        table.setMaxHeight(Region.USE_PREF_SIZE);

        return table;
    }

    private static LineChart<Number, Number> buildBalanceChart(List<BalanceHistoryPoint> history) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Step");
        xAxis.setTickUnit(1);
        xAxis.setMinorTickVisible(false);
        xAxis.setTickLabelFormatter(Format.integerAxisFormatter());
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Balance");

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Account balance over time");
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setPrefHeight(200);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for (BalanceHistoryPoint point : history)
            series.getData().add(new XYChart.Data<>(point.step(), point.balance()));
        chart.getData().add(series);

        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(Math.max(history.size() - 1, 1));

        return chart;
    }

    private static class Snapshot {
        private final List<UserSummary> users;
        private final Set<String> mmUsernames;
        private final UserDetails details;
        private final List<BalanceLedgerEntry> ledger;
        private final List<BalanceHistoryPoint> balanceHistory;
        private final String accountError;

        private Snapshot(List<UserSummary> users, Set<String> mmUsernames, UserDetails details,
                         List<BalanceLedgerEntry> ledger, List<BalanceHistoryPoint> balanceHistory, String accountError) {
            this.users = users;
            this.mmUsernames = mmUsernames;
            this.details = details;
            this.ledger = ledger;
            this.balanceHistory = balanceHistory;
            this.accountError = accountError;
        }
    }

    public void refresh() {
        Background.fetch(this::fetchSnapshot, this::show, Exception::printStackTrace);
    }

    private Snapshot fetchSnapshot() {
        List<UserSummary> users = engine.getAllUsers();
        Set<String> mmUsernames = engine.getAllEvents().stream().map(EventDetails::mmUsername).collect(Collectors.toSet());
        try {
            return new Snapshot(users, mmUsernames, engine.getUserDetails(currentUsername),
                    engine.getUserBalanceLedger(currentUsername), engine.getUserBalanceHistory(currentUsername), null);
        } catch (GuessMarketException exception) {
            return new Snapshot(users, mmUsernames, null, null, null, exception.getMessage());
        }
    }

    private void show(Snapshot snapshot) {
        List<UserSummary> latest = snapshot.users;
        mmUsernames = snapshot.mmUsernames;

        List<UserSummary> others = latest.stream().filter(user -> !user.username().equals(currentUsername)).toList();

        for (UserSummary source : others) {
            rows.stream().filter(row -> row.username().equals(source.username())).findFirst()
                    .ifPresentOrElse(row -> row.update(source), () -> rows.add(new UserRow(source)));
        }
        rows.removeIf(row -> others.stream().noneMatch(source -> source.username().equals(row.username())));
        table.refresh();

        showOwnAccount(snapshot);
    }
}
