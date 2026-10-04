package Client.Views;

import Models.External.GuessMarketException;
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

        try {
            engine.registerUser(username);
            onSuccess.accept(engine, username);
        } catch (GuessMarketException exception) {
            errorLabel.setText(exception.getMessage());
        }
    }
}
