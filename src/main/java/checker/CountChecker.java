package checker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.ESyncConfig;

public class CountChecker extends AbstractChecker {
    private static final Logger log = LoggerFactory
            .getLogger(CountChecker.class);

    public CountChecker(ESyncConfig config) {
        super(config);
    }

    @Override
    void check() {
        log.info("Checking count ...");
        log.info(String.format("Total number of documents in db: %d", db.getTotalCountDocument()));
        log.info(String.format("Total number of documents in es:: %d", es.getTotalCountDocument()));


    }
}
