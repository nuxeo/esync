package checker;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import config.ESyncConfig;
import event.ErrorEvent;

public class TypeCardinalityChecker extends AbstractChecker {
    private static final Logger log = LoggerFactory
            .getLogger(TypeCardinalityChecker.class);

    public TypeCardinalityChecker(ESyncConfig config, EventBus eventBus) {
        super(config, eventBus);
    }

    @Override
    void check() {
        Map<String, Integer> esTypes = es.getTypeCardinality();
        Map<String, Integer> dbTypes = db.getTypeCardinality();
        for (String key : dbTypes.keySet()) {
            postMessage(String.format("db %s: %d", key, dbTypes.get(key)));
        }
    }

    @Override
    String getName() {
        return "TypeCardinalityChecker";
    }
}
