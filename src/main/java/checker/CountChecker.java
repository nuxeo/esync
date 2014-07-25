package checker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import config.ESyncConfig;

public class CountChecker extends AbstractChecker {
    private static final Logger log = LoggerFactory
            .getLogger(CountChecker.class);

    public CountChecker(ESyncConfig config, EventBus eventBus) {
        super(config, eventBus);
    }

    @Override
    void check() {
        postMessage(String.format("Total number of documents in db: %d",
                db.getTotalCountDocument()));
        postMessage(String.format("Total number of documents in es:: %d",
                es.getTotalCountDocument()));
    }

    @Override
    String getName() {
        return "CountChecker";
    }
}