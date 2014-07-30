package listener;

import event.ErrorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import event.InfoEvent;

public class ErrorListener {
    private static final Logger log = LoggerFactory
            .getLogger(ErrorListener.class);

    @Subscribe
    public void handleEvent(ErrorEvent event) {
        log.error(event.getMessage());
    }
}
