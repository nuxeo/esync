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
        if (dbCount == esCount) {
            postMessage(String.format("Same number of documents found: %d",
                    dbCount));
        } else {
            postError(String
                    .format("Different number of documents, expected %d actual %d, diff=%d",
                            dbCount, esCount, dbCount - esCount));
        }
    }

    @Override
    String getName() {
        return "CardinalityChecker";
    }
}
