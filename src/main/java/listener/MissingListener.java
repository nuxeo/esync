package listener;

import event.MissingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public class MissingListener {
    private static final Logger log = LoggerFactory
            .getLogger(MissingListener.class);

    @Subscribe
    public void handleEvent(MissingEvent event) {
        log.error(String.format("MISS: %s, %s", event.getDocId(),
                event.getMessage()));
    }
}
