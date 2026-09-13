package JavaFX.Views;

import Engine.External.*;
import Engine.GuessMarketEngine;
import JavaFX.Format;
import JavaFX.Views.Dialogs.AlertUtils;
import JavaFX.Views.Dialogs.BuySharesDialog;
import JavaFX.Views.Dialogs.CloseEventDialog;
import JavaFX.Views.Dialogs.OpenEventDialog;
import JavaFX.Views.Dialogs.SubmitOrderDialog;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

class EventDetailPane extends BorderPane {
    private final GuessMarketEngine engine;
    private final int eventId;
    private final String contextUsername;
    private final Runnable onDataChanged;

    public EventDetailPane(GuessMarketEngine engine, int eventId, String contextUsername, Runnable onDataChanged) {
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
            if (event.method() == TradingMethod.LMSR) {
                EventStatus status = engine.getEventStatus(event.id());
                setCenter(LmsrEventDetailPane.build(status));
            } else {
                OrderBookStatus status = engine.getOrderBookStatus(event.id());
                setCenter(OrderBookEventDetailPane.build(status));
            }
        } catch (GuessMarketException exception) {
            AlertUtils.showError("Could not load event status", exception.getMessage());
        }
    }

    private void afterAction() {
        onDataChanged.run();
    }
}
