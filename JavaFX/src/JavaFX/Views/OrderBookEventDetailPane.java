package JavaFX.Views;

import Engine.External.OptionMarketData;
import Engine.External.OrderBookStatus;
import Engine.External.RestingOrderView;
import Engine.External.TradeHistoryRecord;
import JavaFX.Format;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

class OrderBookEventDetailPane {
    public static Node build(OrderBookStatus status) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        FlowPane books = new FlowPane(12, 12);
        for (OptionMarketData option : status.options())
            books.getChildren().add(buildOptionBookPanel(option));
        root.getChildren().add(books);

        Label participantsTitle = new Label("Trade history (most recent first):");
        participantsTitle.setStyle("-fx-font-weight: bold;");
        root.getChildren().add(participantsTitle);

        if (status.history().isEmpty()) {
            root.getChildren().add(new Label("(no trades yet)"));
        } else {
            for (TradeHistoryRecord trade : status.history()) {
                String counterparty = trade.minted()
                        ? " (minted)"
                        : " with " + trade.sellerUsername();
                root.getChildren().add(new Label(trade.buyerUsername() + " bought " + trade.quantity() + " \"" + trade.optionName()
                        + "\" @ " + Format.decimal(trade.price()) + counterparty));
            }
        }

        root.getChildren().add(new Label("Event account balance: " + Format.decimal(status.accountBalance())));
        root.getChildren().add(new Label("Total fee collected: " + Format.decimal(status.totalFeeCollected())));

        if (status.winningOptionName() != null)
            root.getChildren().add(new Label("Winning option: " + status.winningOptionName()));

        MFXScrollPane scrollPane = new MFXScrollPane(root);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    private static Node buildOptionBookPanel(OptionMarketData option) {
        VBox panel = new VBox(6);
        panel.setPadding(new Insets(8));
        panel.setStyle("-fx-border-color: gray; -fx-border-radius: 4;");
        panel.setPrefWidth(280);
        VBox.setVgrow(panel, Priority.ALWAYS);

        Label title = new Label(option.optionName());
        title.setStyle("-fx-font-weight: bold;");
        panel.getChildren().add(title);

        panel.getChildren().add(new Label("Last: " + nullableDecimal(option.lastTradePrice())
                + "   Bid: " + nullableDecimal(option.bestBid()) + "   Ask: " + nullableDecimal(option.bestAsk())));
        panel.getChildren().add(new Label("Mid: " + nullableDecimal(option.mid()) + "   Spread: " + nullableDecimal(option.spread())));

        panel.getChildren().add(new Label("Bids:"));
        for (RestingOrderView order : option.bids())
            panel.getChildren().add(new Label("   " + order.username() + "  qty " + order.quantity() + " @ " + Format.decimal(order.price())));

        panel.getChildren().add(new Label("Asks:"));
        for (RestingOrderView order : option.asks())
            panel.getChildren().add(new Label("   " + order.username() + "  qty " + order.quantity() + " @ " + Format.decimal(order.price())));

        return panel;
    }

    private static String nullableDecimal(Double value) {
        return value == null ? "-" : Format.decimal(value);
    }
}
