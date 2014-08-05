package checker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import event.ErrorEvent;

public class CardinalityChecker extends AbstractChecker {
    private static final Logger log = LoggerFactory
            .getLogger(CardinalityChecker.class);

    @Override
    void check() {
        long esCount = es.getCardinality();
        postMessage(String.format("Total number of documents in es:: %d",
                esCount));
        long dbCount = db.getCardinality();
        postMessage(String.format("Total number of documents in db: %d",
                dbCount));
        if (dbCount != esCount) {
            post(new ErrorEvent(
                    String.format(
                            "Different number of documents between DB and ES: %d vs %d",
                            dbCount, esCount)));
        }
    }

    @Override
    String getName() {
        return "CardinalityChecker";
    }
}
