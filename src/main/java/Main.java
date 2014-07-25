import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import checker.AbstractChecker;
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
import sun.security.krb5.Config;


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
        List<Runnable> checkers = new ArrayList<>();
        checkers.add(new AclChecker(config, eventBus));
        checkers.add(new CountChecker(config, eventBus));
        ExecutorService pool = Executors.newFixedThreadPool(config.getPoolSize());
        for (Runnable checker: checkers) {
            pool.execute(checker);
        }
        pool.shutdown();
    }

    private static void registerListener() {
        eventBus = new EventBus();
        eventBus.register(new InfoListener());
        eventBus.register(new TrailingListener());
        eventBus.register(new MissingListener());
        eventBus.register(new DiffListener());
    }

}
