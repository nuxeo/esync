package checker;

import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import event.ErrorEvent;

public class TypeCardinalityChecker extends AbstractChecker {
    private static final Logger log = LoggerFactory
            .getLogger(TypeCardinalityChecker.class);

    @Override
    void check() {
        LinkedHashMap<String, Long> esTypes = es.getTypeCardinality();
        LinkedHashMap<String, Long> dbTypes = db.getTypeCardinality();
        MapDifference<String, Long> diff = Maps.difference(dbTypes, esTypes);
        if (diff.areEqual()) {
            postMessage("Found same type cadinality on db and es");
        }
        for (String key : diff.entriesOnlyOnLeft().keySet()) {
            post(new ErrorEvent(String.format("Missing type on ES: %s (%d)",
                    key, dbTypes.get(key))));
        }
        for (String key : diff.entriesOnlyOnRight().keySet()) {
            post(new ErrorEvent(String.format(
                    "Unknown type on ES not present on db: %s (%d)", key,
                    dbTypes.get(key))));
        }
        if (!diff.areEqual()) {
            postMessage("Difference found in types cadinality");
            for (String key : diff.entriesDiffering().keySet()) {
                long esCount = 0;
                long dbCount = 0;
                if (esTypes.containsKey(key)) {
                    esCount = esTypes.get(key);
                }
                if (dbTypes.containsKey(key)) {
                    dbCount = dbTypes.get(key);
                }
                post(new ErrorEvent(
                        String.format(
                                "Different number of %s documents: %d in db vs %d in es",
                                key, dbCount, esCount)));
            }
        }
    }

    @Override
    String getName() {
        return "TypeCardinalityChecker";
    }
}
