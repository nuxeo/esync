package checker;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import config.ESyncConfig;
import event.ErrorEvent;

public class CountChecker extends AbstractChecker {
    private static final Logger log = LoggerFactory
            .getLogger(CountChecker.class);

    public CountChecker(ESyncConfig config, EventBus eventBus) {
        super(config, eventBus);
    }

    @Override
    void check() {
        checkCardinality();
        checkTypeCardinality();
    }

    private void checkTypeCardinality() {
        Map<String, Integer> esTypes = es.getTypeCardinality();
        Map<String, Integer> dbTypes = db.getTypeCardinality();
        for (String key : dbTypes.keySet()) {
            postMessage(String.format("db %s: %d", key, dbTypes.get(key)));
        }
    }

    private void checkCardinality() {
        long esCount = es.getCardinality();
        postMessage(String.format("Total number of documents in es:: %d",
                esCount));
        long dbCount = db.getCardinality();
        postMessage(String.format("Total number of documents in db: %d",
                dbCount));
        if (dbCount != esCount) {
            post(new ErrorEvent(
                    String.format(
                            ""
                                    + "Different number of documents between DB and ES: %d vs %d",
                            dbCount, esCount)));
        }
    }

    @Override
    String getName() {
        return "CountChecker";
    }
}
