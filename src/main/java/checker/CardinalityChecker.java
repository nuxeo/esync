package checker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CardinalityChecker extends AbstractChecker {
    private static final Logger log = LoggerFactory
            .getLogger(CardinalityChecker.class);

    @Override
    void check() {
        long esCount = es.getCardinality();
        long dbCount = db.getCardinality();
        compare(esCount, dbCount, "documents (except: versions, proxies and Root)");
        esCount = es.getProxyCardinality();
        dbCount = db.getProxyCardinality();
        compare(esCount, dbCount, "proxy documents");
        esCount = es.getVersionCardinality();
        dbCount = db.getVersionCardinality();
        compare(esCount, dbCount, "version documents");
        esCount = es.getOrphanCardinality();
        dbCount = db.getOrphanCardinality();
        compare(esCount, dbCount, "orphan documents (except: versions)");
    }

    private void compare(long esCount, long dbCount, String message) {
        if (dbCount == esCount) {
            postMessage(String.format("Same number of %s: %d",
                    message, dbCount));
        } else {
            postError(String
                    .format("Different number of %s, expected %d actual %d, diff=%d",
                            message, dbCount, esCount, dbCount - esCount));
        }
    }

    @Override
    String getName() {
        return "CardinalityChecker";
    }
}
