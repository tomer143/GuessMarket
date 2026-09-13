package JavaFX.Tasks;

import Engine.GuessMarketEngine;
import javafx.concurrent.Task;

public class LoadEventsFileTask extends Task<Void> {
    private final GuessMarketEngine engine;
    private final String path;

    public LoadEventsFileTask(GuessMarketEngine engine, String path) {
        this.engine = engine;
        this.path = path;
    }

    @Override
    protected Void call() throws Exception {
        updateMessage("Reading file...");
        updateProgress(0, 1);
        Thread.sleep(700);

        updateProgress(0.4, 1);
        updateMessage("Loading events...");
        engine.loadEventsFile(path);

        updateProgress(0.8, 1);
        Thread.sleep(700);
        updateProgress(1, 1);
        updateMessage("Done.");

        return null;
    }
}
