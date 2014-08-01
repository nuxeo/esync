package listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import es.EsHash;
import event.DiffEvent;

public class DiffListener {
    private static final Logger log = LoggerFactory
            .getLogger(DiffListener.class);

    @Subscribe
    public void handleEvent(DiffEvent event) {
        log.error((String.format("DIFF: %s, es: %s, %s shard %d",
                event.getDbDocument(), event.getEsDocument(),
                event.getMessage(), EsHash.getShard(event.getEsDocument().id))));
    }
}
