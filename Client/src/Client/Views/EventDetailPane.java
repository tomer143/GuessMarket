package Client.Views;

import Engine.ClientGuessMarketEngine;
import Client.Format;
import Client.Tasks.Background;
import Client.Views.Dialogs.AlertUtils;
import Client.Views.Dialogs.BuySharesDialog;
import Client.Views.Dialogs.CloseEventDialog;
import Client.Views.Dialogs.OpenEventDialog;
import Client.Views.Dialogs.SubmitOrderDialog;
import Models.External.*;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

class EventDetailPane extends BorderPane {
    private double dividerPosition = 0.68;

    private final ClientGuessMarketEngine engine;
    private final int eventId;
    private final String contextUsername;
    private final Runnable onDataChanged;

    public EventDetailPane(ClientGuessMarketEngine engine, int eventId, String contextUsername, Runnable onDataChanged) {
        this.engine = engine;
        this.eventId = eventId;
        this.contextUsername = contextUsername;
        this.onDataChanged = onDataChanged;
        setCenter(new Label("Loading..."));
        refresh();
    }

    private static class Snapshot {
        private final EventDetails event;
        private final EventStatus lmsrStatus;
        private final LmsrParticipation lmsrParticipation;
        private final OrderBookStatus orderBookStatus;
        private final OrderBookParticipation orderBookParticipation;
        private final String error;

        private Snapshot(EventDetails event, EventStatus lmsrStatus, LmsrParticipation lmsrParticipation,
                         OrderBookStatus orderBookStatus, OrderBookParticipation orderBookParticipation, String error) {
            this.event = event;
            this.lmsrStatus = lmsrStatus;
            this.lmsrParticipation = lmsrParticipation;
            this.orderBookStatus = orderBookStatus;
            this.orderBookParticipation = orderBookParticipation;
            this.error = error;
        }
    }

    public void refresh() {
        Background.fetch(this::fetchSnapshot, this::show, Exception::printStackTrace);
    }

    private Snapshot fetchSnapshot() {
        EventDetails event = engine.getAllEvents().stream().filter(e -> e.id() == eventId).findFirst().orElse(null);
        if (event == null)
            return new Snapshot(null, null, null, null, null, null);

        try {
            if (event.method() == TradingMethod.LMSR) {
                EventStatus status = engine.getEventStatus(event.id());
                LmsrParticipation participation = contextUsername == null ? null : engine.getUserLmsrParticipation(event.id(), contextUsername);
                return new Snapshot(event, status, participation, null, null, null);
            }
            OrderBookStatus status = engine.getOrderBookStatus(event.id());
            OrderBookParticipation participation = contextUsername == null ? null : engine.getUserOrderBookParticipation(event.id(), contextUsername);
            return new Snapshot(event, null, null, status, participation, null);
        } catch (GuessMarketException exception) {
            return new Snapshot(event, null, null, null, null, exception.getMessage());
        }
    }

    private void show(Snapshot snapshot) {
        EventDetails event = snapshot.event;
        if (event == null) {
            setCenter(new Label("This event is no longer available."));
            return;
        }

        VBox header = new VBox(4);
        header.setPadding(new Insets(10));
        Label title = new Label(event.name() + "  (id " + event.id() + ")");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        header.getChildren().add(title);
        header.getChildren().add(new Label(event.description()));
        header.getChildren().add(new Label("Type: " + Format.method(event.method())
                + "   Status: " + Format.phase(event.phase())
                + "   Fee: " + event.feePercent() + "% (" + Format.feeCollection(event.feeCollection()) + ")"
                + "   Market maker: " + event.mmUsername()));

        HBox actions = new HBox(8);
        actions.setPadding(new Insets(0, 10, 10, 10));

        if (event.phase() == EventPhase.NOT_ACTIVE) {
            MFXButton openButton = new MFXButton("Open Event");
            openButton.getStyleClass().add("bordered-button");
            openButton.setOnAction(e -> OpenEventDialog.show(engine, event, this::afterAction));
            actions.getChildren().add(openButton);
        } else if (event.phase() == EventPhase.ACTIVE) {
            MFXButton tradeButton = new MFXButton(event.method() == TradingMethod.LMSR ? "Buy Shares" : "Submit Order");
            tradeButton.getStyleClass().add("bordered-button");
            tradeButton.setOnAction(e -> {
                if (event.method() == TradingMethod.LMSR)
                    BuySharesDialog.show(engine, event, contextUsername, this::afterAction);
                else
                    SubmitOrderDialog.show(engine, event, contextUsername, this::afterAction);
            });
            MFXButton closeButton = new MFXButton("Close Event");
            closeButton.getStyleClass().add("bordered-button");
            closeButton.setOnAction(e -> CloseEventDialog.show(engine, event, this::afterAction));
            actions.getChildren().addAll(tradeButton, closeButton);
        }

        setTop(new VBox(header, actions));

        if (snapshot.error != null) {
            AlertUtils.showErrorOnce("Could not load event status", snapshot.error);
            return;
        }

        Node market;
        Node participation = null;
        if (event.method() == TradingMethod.LMSR) {
            market = LmsrEventDetailPane.build(snapshot.lmsrStatus);
            if (snapshot.lmsrParticipation != null)
                participation = LmsrEventDetailPane.buildParticipation(snapshot.lmsrParticipation);
        } else {
            market = OrderBookEventDetailPane.build(snapshot.orderBookStatus);
            if (snapshot.orderBookParticipation != null)
                participation = OrderBookEventDetailPane.buildParticipation(snapshot.orderBookParticipation);
        }

        if (participation == null) {
            setCenter(market);
        } else {
            Label participationTitle = new Label("Participations information");
            participationTitle.setStyle("-fx-font-weight: bold;");
            participationTitle.setPadding(new Insets(0, 10, 0, 10));

            VBox participationBox = new VBox(6, participationTitle, participation);
            VBox.setVgrow(participation, Priority.ALWAYS);

            if (getCenter() instanceof SplitPane previous && previous.getDividers().size() == 1)
                dividerPosition = previous.getDividerPositions()[0];

            SplitPane split = new SplitPane(market, participationBox);
            split.setOrientation(Orientation.VERTICAL);
            split.setDividerPositions(dividerPosition);
            setCenter(split);
        }
        AlertUtils.clearReportedError("Could not load event status");
    }

    private void afterAction() {
        onDataChanged.run();
    }
}
