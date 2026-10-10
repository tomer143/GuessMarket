package Client.Views;

import Client.Tasks.Background;
import Engine.ClientGuessMarketEngine;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.function.BiConsumer;

public class LoginController {
    @FXML private MFXTextField usernameField;
    @FXML private MFXButton loginButton;
    @FXML private Label errorLabel;

    private ClientGuessMarketEngine engine;
    private BiConsumer<ClientGuessMarketEngine, String> onSuccess;

    public void init(ClientGuessMarketEngine engine, BiConsumer<ClientGuessMarketEngine, String> onSuccess) {
        this.engine = engine;
        this.onSuccess = onSuccess;
    }

    @FXML
    private void onLoginClicked() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        errorLabel.setText(null);
        loginButton.setDisable(true);

        Background.fetch(() -> engine.registerUser(username), storedUsername -> onSuccess.accept(engine, storedUsername), exception -> {
            loginButton.setDisable(false);
            errorLabel.setText(exception.getMessage());
        });
    }
}
