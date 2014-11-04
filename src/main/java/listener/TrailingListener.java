package listener;

import event.TrailingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public class TrailingListener {
    private static final Logger log = LoggerFactory
            .getLogger(TrailingListener.class);

    @Subscribe
    public void handleEvent(TrailingEvent event) {
        log.error(String.format("REMOVE: %s, %s", event.getDocId(),
                event.getMessage()));
    }
}
