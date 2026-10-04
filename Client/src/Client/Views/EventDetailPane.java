package Client.Views;

import Engine.ClientGuessMarketEngine;
import Client.Format;
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
        refresh();
    }

    public void refresh() {
        EventDetails event = engine.getAllEvents().stream().filter(e -> e.id() == eventId).findFirst().orElse(null);
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

        try {
            Node market;
            Node participation = null;
            if (event.method() == TradingMethod.LMSR) {
                market = LmsrEventDetailPane.build(engine.getEventStatus(event.id()));
                if (contextUsername != null)
                    participation = LmsrEventDetailPane.buildParticipation(engine.getUserLmsrParticipation(event.id(), contextUsername));
            } else {
                market = OrderBookEventDetailPane.build(engine.getOrderBookStatus(event.id()));
                if (contextUsername != null)
                    participation = OrderBookEventDetailPane.buildParticipation(engine.getUserOrderBookParticipation(event.id(), contextUsername));
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
        } catch (GuessMarketException exception) {
            AlertUtils.showErrorOnce("Could not load event status", exception.getMessage());
        }
    }

    private void afterAction() {
        onDataChanged.run();
    }
}
