package listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public class InfoListener {
    private static final Logger log = LoggerFactory
            .getLogger(InfoListener.class);

    @Subscribe
    public void handleEvent(InfoEvent event) {
        log.info(event.getMessage());
    }
}
