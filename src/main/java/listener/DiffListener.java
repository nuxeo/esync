package listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public class DiffListener {
    private static final Logger log = LoggerFactory
            .getLogger(DiffListener.class);

    @Subscribe
    public void handleEvent(DiffEvent event) {
        log.info(String.format("DIFF: %s, es: %s, %s", event.getDbDocument(),
                event.getEsDocument(), event.getMessage()));
    }
}
