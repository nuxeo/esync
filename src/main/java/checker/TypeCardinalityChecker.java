package checker;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import event.DiffTypeEvent;

public class TypeCardinalityChecker extends AbstractChecker {
    private static final Logger log = LoggerFactory
            .getLogger(TypeCardinalityChecker.class);

    @Override
    void check() {
        Map<String, Long> esTypes = es.getTypeCardinality();
        Map<String, Long> dbTypes = db.getTypeCardinality();
        MapDifference<String, Long> diff = Maps.difference(dbTypes, esTypes);
        if (diff.areEqual()) {
            postMessage("Found same types cardinality");
            return;
        }
        postMessage("Difference found in types cardinality.");
        for (String key : diff.entriesOnlyOnLeft().keySet()) {
            postError(String.format("Missing type on ES: %s, expected: %d",
                    key, dbTypes.get(key)));
        }
        for (String key : diff.entriesOnlyOnRight().keySet()) {
            postError(String.format("Spurious type in ES: %s, actual: %d", key,
                    esTypes.get(key)));
        }
        for (String key : diff.entriesDiffering().keySet()) {
            long esCount = 0;
            long dbCount = 0;
            if (esTypes.containsKey(key)) {
                esCount = esTypes.get(key);
            }
            if (dbTypes.containsKey(key)) {
                dbCount = dbTypes.get(key);
            }
            postError(String.format(
                    "Document type %s (including versions), expected: %d, actual: %d, diff: %d", key, dbCount,
                    esCount, dbCount - esCount));
            post(new DiffTypeEvent(key, "diff"));
        }
    }

    @Override
    String getName() {
        return "TypeCardinalityChecker";
    }
}
