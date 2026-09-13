package JavaFX.Views;

import Engine.External.EventStatus;
import Engine.External.OptionStatus;
import Engine.External.TradeRecord;
import JavaFX.Format;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

class LmsrEventDetailPane {
    public static javafx.scene.Node build(EventStatus status) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        for (OptionStatus optionStatus : status.optionStatuses()) {
            Label line = new Label(optionStatus.name() + ":  chance " + Format.decimal(optionStatus.chance())
                    + "   |   total shares bought " + optionStatus.totalSharesBought());
            root.getChildren().add(line);
        }

        root.getChildren().add(new Label("Event account balance: " + Format.decimal(status.accountBalance())));
        root.getChildren().add(new Label("Total fee collected: " + Format.decimal(status.totalFeeCollected())));

        if (status.winningOptionName() != null)
            root.getChildren().add(new Label("Winning option: " + status.winningOptionName()));

        Label historyTitle = new Label("Trade history (most recent first):");
        historyTitle.setStyle("-fx-font-weight: bold;");
        root.getChildren().add(historyTitle);

        if (status.history().isEmpty()) {
            root.getChildren().add(new Label("(no trades yet)"));
        } else {
            for (TradeRecord trade : status.history()) {
                root.getChildren().add(new Label("Bought " + trade.amount() + " share(s) of \"" + trade.optionName()
                        + "\" for " + Format.decimal(trade.pricePaid())));
            }
        }

        MFXScrollPane scrollPane = new MFXScrollPane(root);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }
}
