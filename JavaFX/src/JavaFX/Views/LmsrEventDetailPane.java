package JavaFX.Views;

import Engine.External.EventStatus;
import Engine.External.OptionStatus;
import Engine.External.TradeRecord;
import JavaFX.Animations;
import JavaFX.Format;
import io.github.palexdev.materialfx.controls.MFXProgressBar;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class LmsrEventDetailPane {
    private static final Color LOW_COLOR = Color.web("#e53935");
    private static final Color MID_COLOR = Color.web("#fdd835");
    private static final Color HIGH_COLOR = Color.web("#43a047");

    public static javafx.scene.Node build(EventStatus status) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        for (OptionStatus optionStatus : status.optionStatuses()) {
            Label line = new Label(optionStatus.name() + ":  chance " + Format.decimal(optionStatus.chance())
                    + "   |   total shares bought " + optionStatus.totalSharesBought());

            MFXProgressBar chanceBar = new MFXProgressBar();
            chanceBar.setPrefWidth(220);
            chanceBar.setProgress(0);
            wireChanceColor(chanceBar);
            Animations.animateProgress(chanceBar.progressProperty(), optionStatus.chance(), Duration.millis(600));

            root.getChildren().addAll(line, chanceBar);
        }

        root.getChildren().add(buildPriceChart(status));

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

    private static Node buildPriceChart(EventStatus status) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Trade #");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Price per share");

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Price history");
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.setPrefHeight(220);

        List<TradeRecord> chronological = new ArrayList<>(status.history());
        Collections.reverse(chronological);

        Map<String, XYChart.Series<Number, Number>> seriesByOption = new LinkedHashMap<>();
        Map<String, Integer> tradeCountByOption = new HashMap<>();
        for (TradeRecord trade : chronological) {
            XYChart.Series<Number, Number> series = seriesByOption.computeIfAbsent(trade.optionName(), name -> {
                XYChart.Series<Number, Number> newSeries = new XYChart.Series<>();
                newSeries.setName(name);
                return newSeries;
            });

            int index = tradeCountByOption.merge(trade.optionName(), 1, Integer::sum) - 1;
            double pricePerShare = trade.amount() == 0 ? 0 : trade.pricePaid() / trade.amount();
            series.getData().add(new XYChart.Data<>(index, pricePerShare));
        }
        chart.getData().addAll(seriesByOption.values());

        return chart;
    }

    private static void wireChanceColor(MFXProgressBar chanceBar) {
        if (chanceBar.getScene() != null) {
            Platform.runLater(() -> attachChanceColorListener(chanceBar));
        } else {
            chanceBar.sceneProperty().addListener(new javafx.beans.value.ChangeListener<>() {
                @Override
                public void changed(javafx.beans.value.ObservableValue<? extends javafx.scene.Scene> observable,
                                     javafx.scene.Scene oldScene, javafx.scene.Scene newScene) {
                    if (newScene == null) return;
                    chanceBar.sceneProperty().removeListener(this);
                    Platform.runLater(() -> attachChanceColorListener(chanceBar));
                }
            });
        }
    }

    private static void attachChanceColorListener(MFXProgressBar chanceBar) {
        Shape bar = (Shape) chanceBar.lookup(".bar1");
        if (bar == null) return;

        applyChanceColor(bar, chanceBar.getProgress());
        chanceBar.progressProperty().addListener((observable, oldValue, newValue) -> applyChanceColor(bar, newValue.doubleValue()));
    }

    private static void applyChanceColor(Shape bar, double progress) {
        bar.setStyle("-fx-fill: " + toWeb(colorFor(progress)) + ";");
    }

    private static Color colorFor(double progress) {
        double clamped = Math.max(0, Math.min(1, progress));
        return clamped < 0.5
                ? LOW_COLOR.interpolate(MID_COLOR, clamped / 0.5)
                : MID_COLOR.interpolate(HIGH_COLOR, (clamped - 0.5) / 0.5);
    }

    private static String toWeb(Color color) {
        return String.format("#%02X%02X%02X", (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255), (int) Math.round(color.getBlue() * 255));
    }
}
