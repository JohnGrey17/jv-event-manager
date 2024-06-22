package mate.academy;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventManager {
    private static final Logger LOGGER = Logger.getLogger(EventManager.class.getName());
    private final List<EventListener> listeners = new CopyOnWriteArrayList<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public void registerListener(EventListener listener) {
        listeners.add(listener);
    }

    public void deregisterListener(EventListener listener) {
        listeners.remove(listener);
    }

    public void notifyEvent(Event event) {
        for (EventListener listener : listeners) {
            try {
                executorService.submit(() -> listener.onEvent(event));
            } catch (RejectedExecutionException e) {
                LOGGER.log(Level.SEVERE, "Task submission rejected: ", e);
            }
        }
    }

    public void shutdown() {
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    LOGGER.severe("ExecutorService did not terminate.");
                }
            }
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Shutdown interrupted: ", e);
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
