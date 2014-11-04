package checker;

import java.util.Set;

import event.MissingEvent;
import event.TrailingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import event.DiffTypeEvent;
import event.ErrorEvent;

public class DocumentTypeLister extends AbstractChecker {
    private static final Logger log = LoggerFactory
            .getLogger(DocumentTypeLister.class);
    private String primaryType;

    @Override
    void check() {
        Set<String> esIds = es.getDocumentIdsForType(primaryType);
        Set<String> dbIds = db.getDocumentIdsForType(primaryType);
        Sets.SetView<String> diff = Sets.difference(dbIds, esIds);
        if (diff.isEmpty()) {
            postMessage(String.format("All DB docIds found in ES for type %s",
                    primaryType));
        } else {
            postMessage(String.format("Missing %d documents for type: %s",
                    diff.size(), primaryType));
            for (String key : diff) {
                post(new MissingEvent(key, String.format("type %s", primaryType)));
            }
        }
        diff = Sets.difference(esIds, dbIds);
        if (diff.isEmpty()) {
            postMessage(String.format("All ES docIds found in DB for type %s",
                    primaryType));
        } else {
            postMessage(String.format("%d spurious documents for type: %s",
                    diff.size(), primaryType));
            for (String key : diff) {
                post(new TrailingEvent(key, String.format("type %s", primaryType)));
            }
        }
    }

    @Override
    public boolean autoRun() {
        // run only when receiving an event
        return false;
    }

    @Subscribe
    public void handleEvent(DiffTypeEvent event) {
        primaryType = event.getType();
        run();
    }

    @Override
    public void init() {
        eventBus.register(this);
    }

    @Override
    String getName() {
        return "DocumentTypeLister";
    }
}
