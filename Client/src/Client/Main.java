package Client;

import Engine.ClientGuessMarketEngine;
import Client.Views.LoginController;
import Client.Views.RootLayoutController;
import io.github.palexdev.materialfx.MFXResourcesLoader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {
    private static final String LOGIN_VIEW_FXML_PATH = "Views/LoginView.fxml";
    private static final String ROOT_LAYOUT_FXML_PATH = "Views/RootLayout.fxml";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Guess Market");
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(450);

        showLogin(primaryStage);
        primaryStage.show();
    }

    private void showLogin(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(LOGIN_VIEW_FXML_PATH);
        fxmlLoader.setLocation(url);
        Parent root = fxmlLoader.load(url.openStream());

        LoginController controller = fxmlLoader.getController();
        controller.init(new ClientGuessMarketEngine(), (engine, username) -> {
            try {
                showRootLayout(primaryStage, engine, username);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        });

        setScene(primaryStage, root, 480, 360);
    }

    private void showRootLayout(Stage primaryStage, ClientGuessMarketEngine engine, String username) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(ROOT_LAYOUT_FXML_PATH);
        fxmlLoader.setLocation(url);
        Parent root = fxmlLoader.load(url.openStream());

        RootLayoutController controller = fxmlLoader.getController();
        controller.init(engine, username, () -> {
            try {
                showLogin(primaryStage);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        });

        setScene(primaryStage, root, 1100, 700);
    }

    private void setScene(Stage primaryStage, Parent root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(MFXResourcesLoader.load("css/DefaultTheme.css"));
        scene.getStylesheets().add(getClass().getResource("Views/app.css").toExternalForm());
        primaryStage.setScene(scene);
    }
}
