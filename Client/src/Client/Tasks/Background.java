package Client.Tasks;

import javafx.application.Platform;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class Background {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "server-requests");
        thread.setDaemon(true);
        return thread;
    });
    
    private static final AtomicInteger pending = new AtomicInteger();

    private Background() {
    }

    public static <T> void fetch(Callable<T> fetcher, Consumer<T> onSuccess, Consumer<Exception> onFailure) {
        pending.incrementAndGet();
        EXECUTOR.execute(() -> {
            try {
                T result = fetcher.call();
                Platform.runLater(() -> onSuccess.accept(result));
            } catch (Exception exception) {
                Platform.runLater(() -> onFailure.accept(exception));
            } finally {
                pending.decrementAndGet();
            }
        });
    }

    public static boolean isIdle() {
        return pending.get() == 0;
    }
}
