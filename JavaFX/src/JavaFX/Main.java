package JavaFX;

import Engine.GuessMarketEngine;
import JavaFX.Views.RootLayoutController;
import io.github.palexdev.materialfx.MFXResourcesLoader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {
    private static final String ROOT_LAYOUT_FXML_PATH = "Views/RootLayout.fxml";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        GuessMarketEngine engine = new GuessMarketEngine();

        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(ROOT_LAYOUT_FXML_PATH);
        fxmlLoader.setLocation(url);
        Parent root = fxmlLoader.load(url.openStream());

        RootLayoutController controller = fxmlLoader.getController();
        controller.init(engine, primaryStage);

        Scene scene = new Scene(root, 1100, 700);
        scene.getStylesheets().add(MFXResourcesLoader.load("css/DefaultTheme.css"));
        scene.getStylesheets().add(getClass().getResource("Views/app.css").toExternalForm());

        primaryStage.setTitle("Guess Market");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(450);
        primaryStage.show();
    }
}
