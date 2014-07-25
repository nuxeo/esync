import java.io.IOException;
import java.sql.SQLException;

import checker.CountChecker;

import com.google.common.eventbus.EventBus;
import listener.DiffListener;
import listener.InfoListener;
import listener.MissingListener;
import listener.TrailingListener;
import org.aeonbits.owner.ConfigFactory;


import checker.AclChecker;
import config.ESyncConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static EventBus eventBus;


    public static void main(String[] args) throws SQLException, IOException {
        log.info("Starting esync...");
        registerListener();
        runCheckers();
        log.info("End of esync");
    }

    private static void runCheckers() {
        ESyncConfig config = ConfigFactory.create(ESyncConfig.class);
        AclChecker aclChecker = new AclChecker(config, eventBus);
        aclChecker.run();

        CountChecker checker = new CountChecker(config, eventBus);
        checker.run();
    }

    private static void registerListener() {
        eventBus = new EventBus();
        InfoListener infoListener = new InfoListener();
        eventBus.register(infoListener);
        TrailingListener trailingListener = new TrailingListener();
        eventBus.register(trailingListener);
        MissingListener missingListener = new MissingListener();
        eventBus.register(missingListener);
        DiffListener diffListener = new DiffListener();
        eventBus.register(diffListener);
    }

}
