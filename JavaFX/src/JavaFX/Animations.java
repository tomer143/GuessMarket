package JavaFX;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.util.Duration;

public class Animations {
    private static final BooleanProperty enabled = new SimpleBooleanProperty(true);

    public static BooleanProperty enabledProperty() {
        return enabled;
    }

    public static void run(Animation animation, Runnable applyEndState) {
        if (enabled.get()) {
            animation.setOnFinished(event -> applyEndState.run());
            animation.play();
        } else {
            applyEndState.run();
        }
    }

    public static void fadeIn(Node node, Duration duration) {
        node.setOpacity(0);
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setToValue(1);
        run(fade, () -> node.setOpacity(1));
    }

    public static void animateProgress(DoubleProperty property, double targetValue, Duration duration) {
        Timeline timeline = new Timeline(new KeyFrame(duration, new KeyValue(property, targetValue)));
        run(timeline, () -> property.set(targetValue));
    }
}
