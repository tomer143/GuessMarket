package JavaFX.Views;

import Engine.External.*;
import Engine.GuessMarketEngine;
import JavaFX.Animations;
import JavaFX.Format;
import JavaFX.Models.ParticipationRow;
import JavaFX.Models.UserRow;
import JavaFX.Views.Dialogs.AlertUtils;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

public class UsersViewController {
    @FXML private TableView<UserRow> table;
    @FXML private VBox detailContainer;

    private final ObservableList<UserRow> rows = FXCollections.observableArrayList();
    private GuessMarketEngine engine;
    private String selectedUsername;

    @FXML
    private void initialize() {
        table.setItems(rows);
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedUsername = newValue == null ? null : newValue.username();
            showUserDetail(selectedUsername);
        });
    }

    public void init(GuessMarketEngine engine) {
        this.engine = engine;
    }

    private void showUserDetail(String username) {
        if (username == null) {
            detailContainer.getChildren().setAll(new Label("Select a user to see their details."));
            return;
        }

        try {
            UserDetails details = engine.getUserDetails(username);
            UserRow headerRow = new UserRow(new UserSummary(details.username(), details.balance(), details.blocked()));

            Label title = new Label();
            title.textProperty().bind(headerRow.usernameProperty());
            title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            Label balanceLabel = new Label();
            balanceLabel.textProperty().bind(Bindings.createStringBinding(
                    () -> "Account balance: " + Format.decimal(headerRow.balanceProperty().get())
                            + (headerRow.blockedProperty().get() ? "   (BLOCKED)" : ""),
                    headerRow.balanceProperty(), headerRow.blockedProperty()));

            VBox header = new VBox(4, title, balanceLabel);

            ObservableList<ParticipationRow> participations = FXCollections.observableArrayList(
                    details.participations().stream().map(ParticipationRow::new).toList());

            TableView<ParticipationRow> participationsTable = new TableView<>();
            TableColumn<ParticipationRow, String> eventNameColumn = new TableColumn<>("Event");
            eventNameColumn.setCellValueFactory(new PropertyValueFactory<>("eventName"));
            TableColumn<ParticipationRow, TradingMethod> methodColumn = new TableColumn<>("Type");
            methodColumn.setCellValueFactory(new PropertyValueFactory<>("method"));
            methodColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(TradingMethod item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : Format.method(item));
                }
            });
            TableColumn<ParticipationRow, Boolean> roleColumn = new TableColumn<>("Role");
            roleColumn.setCellValueFactory(new PropertyValueFactory<>("mm"));
            roleColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : (item ? "Market maker" : "Participant"));
                }
            });
            participationsTable.getColumns().addAll(eventNameColumn, methodColumn, roleColumn);
            participationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
            participationsTable.setItems(participations);

            double rowHeight = 32;
            int maxVisibleRows = 6;
            participationsTable.setFixedCellSize(rowHeight);
            participationsTable.prefHeightProperty().bind(Bindings.createDoubleBinding(
                    () -> rowHeight * (Math.min(Math.max(participations.size(), 1), maxVisibleRows) + 1) + 2,
                    participations));
            participationsTable.setMinHeight(Region.USE_PREF_SIZE);
            participationsTable.setMaxHeight(Region.USE_PREF_SIZE);

            VBox eventDetailContainer = new VBox();
            VBox.setVgrow(eventDetailContainer, Priority.ALWAYS);
            eventDetailContainer.getChildren().add(new Label("Select an event above to see its details."));

            participationsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, participation) -> {
                if (participation == null) return;
                EventDetailPane pane = new EventDetailPane(engine, participation.eventId(), username);
                VBox.setVgrow(pane, Priority.ALWAYS);
                eventDetailContainer.getChildren().setAll(pane);
            });

            VBox content = new VBox(10, header, participationsTable, eventDetailContainer);
            VBox.setVgrow(content, Priority.ALWAYS);
            detailContainer.getChildren().setAll(content);
            Animations.fadeIn(content, Duration.millis(250));
        } catch (GuessMarketException exception) {
            AlertUtils.showError("Could not load user details", exception.getMessage());
        }
    }

    public void refresh() {
        List<UserSummary> latest = engine.getAllUsers();

        for (UserSummary source : latest) {
            rows.stream().filter(row -> row.username().equals(source.username())).findFirst()
                    .ifPresentOrElse(row -> row.update(source), () -> rows.add(new UserRow(source)));
        }
        rows.removeIf(row -> latest.stream().noneMatch(source -> source.username().equals(row.username())));

        if (selectedUsername != null) showUserDetail(selectedUsername);
    }
}
