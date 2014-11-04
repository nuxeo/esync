package checker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrphanLister extends AbstractChecker {
    private static final Logger log = LoggerFactory
            .getLogger(OrphanLister.class);

    @Override
    void check() {

    }

    @Override
    String getName() {
        return "OrphanLister";
    }
}
