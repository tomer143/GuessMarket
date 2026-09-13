package JavaFX.Views;

import Engine.External.EventDetails;
import Engine.External.EventPhase;
import Engine.External.FeeCollection;
import Engine.External.TradingMethod;
import Engine.GuessMarketEngine;
import JavaFX.Animations;
import JavaFX.Format;
import JavaFX.Models.EventRow;
import JavaFX.Views.Components.FilterButtonGroup;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Duration;

import java.util.List;

public class EventsViewController {
    @FXML private VBox filtersContainer;
    @FXML private TableView<EventRow> table;
    @FXML private TableColumn<EventRow, EventPhase> statusColumn;
    @FXML private TableColumn<EventRow, TradingMethod> typeColumn;
    @FXML private TableColumn<EventRow, Number> feeColumn;
    @FXML private VBox detailContainer;

    private final ObservableList<EventRow> rows = FXCollections.observableArrayList();
    private final FilteredList<EventRow> filteredRows = new FilteredList<>(rows);
    private GuessMarketEngine engine;
    private Runnable onDataChanged;
    private EventDetailPane detailPane;

    @FXML
    private void initialize() {
        statusColumn.setCellFactory(formattedCell(Format::phase));
        typeColumn.setCellFactory(formattedCell(Format::method));
        feeColumn.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                EventRow row = getTableRow().getItem();
                setText(item + "% (" + Format.feeCollection(row.feeCollection()) + ")");
            }
        });

        table.setItems(filteredRows);
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showDetail(newValue));
    }

    public void init(GuessMarketEngine engine, Runnable onDataChanged) {
        this.engine = engine;
        this.onDataChanged = onDataChanged;

        FilterButtonGroup<TradingMethod, EventRow> typeFilter = new FilterButtonGroup<>("Type",
                List.of(TradingMethod.LMSR, TradingMethod.ORDER_BOOK), Format::method, (option, row) -> row.method() == option);
        FilterButtonGroup<EventPhase, EventRow> statusFilter = new FilterButtonGroup<>("Status",
                List.of(EventPhase.NOT_ACTIVE, EventPhase.ACTIVE, EventPhase.CLOSED), Format::phase, (option, row) -> row.phase() == option);
        FilterButtonGroup<FeeCollection, EventRow> feeFilter = new FilterButtonGroup<>("Fee method",
                List.of(FeeCollection.OnPurchase, FeeCollection.OnClose), Format::feeCollection, (option, row) -> row.feeCollection() == option);

        Runnable applyFilters = () -> filteredRows.setPredicate(row ->
                typeFilter.predicate().test(row) && statusFilter.predicate().test(row) && feeFilter.predicate().test(row));
        typeFilter.setOnChange(applyFilters);
        statusFilter.setOnChange(applyFilters);
        feeFilter.setOnChange(applyFilters);

        filtersContainer.getChildren().addAll(typeFilter, statusFilter, feeFilter);
    }

    private <T> Callback<TableColumn<EventRow, T>, javafx.scene.control.TableCell<EventRow, T>> formattedCell(java.util.function.Function<T, String> formatter) {
        return column -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatter.apply(item));
            }
        };
    }

    private void showDetail(EventRow row) {
        if (row == null) {
            detailContainer.getChildren().setAll(new Label("Select an event to see its details."));
            return;
        }
        detailPane = new EventDetailPane(engine, row.id(), null, onDataChanged);
        VBox.setVgrow(detailPane, Priority.ALWAYS);
        detailContainer.getChildren().setAll(detailPane);
        Animations.fadeIn(detailPane, Duration.millis(250));
    }

    public void refresh() {
        List<EventDetails> latest = engine.getAllEvents();

        for (EventDetails source : latest) {
            rows.stream().filter(row -> row.id() == source.id()).findFirst()
                    .ifPresentOrElse(row -> row.update(source), () -> rows.add(new EventRow(source)));
        }
        rows.removeIf(row -> latest.stream().noneMatch(source -> source.id() == row.id()));

        if (detailPane != null) detailPane.refresh();
    }
}
