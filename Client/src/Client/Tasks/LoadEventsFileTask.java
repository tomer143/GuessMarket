package Client.Tasks;

import Engine.ClientGuessMarketEngine;
import javafx.concurrent.Task;

public class LoadEventsFileTask extends Task<Void> {
    private final ClientGuessMarketEngine engine;
    private final String path;

    public LoadEventsFileTask(ClientGuessMarketEngine engine, String path) {
        this.engine = engine;
        this.path = path;
    }

    @Override
    protected Void call() throws Exception {
        updateMessage("Uploading file...");
        updateProgress(0, 1);

        engine.loadEventsFile(path);

        updateProgress(1, 1);
        updateMessage("Done.");

        return null;
    }
}
