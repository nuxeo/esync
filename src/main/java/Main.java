import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import listener.DiffListener;
import listener.ErrorListener;
import listener.InfoListener;
import listener.MissingListener;
import listener.TrailingListener;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import checker.AclChecker;
import checker.CardinalityChecker;
import checker.TypeCardinalityChecker;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.google.common.eventbus.EventBus;
import config.ESyncConfig;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static EventBus eventBus;
    private final static MetricRegistry registry = SharedMetricRegistries
            .getOrCreate("main");
    private final static ConsoleReporter reporter = ConsoleReporter
            .forRegistry(registry).convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS).build();
    private static ESyncConfig config;

    public static void main(String[] args) throws SQLException, IOException {
        log.info("Starting esync...");
        config = getConfig(args);
        registerListener();
        runCheckers();
        log.info("End of esync");
        if (log.isDebugEnabled()) {
            reportMetrics();
        }
    }

    private static ESyncConfig getConfig(String[] args) {
        ESyncConfig ret;
        if (args.length > 0) {
            Properties props = new Properties();
            try {
                props.load(new FileInputStream(args[0]));
            } catch (IOException e) {
                log.error("Wrong file " + args[0] + e.getMessage(), e);
                throw new IllegalArgumentException(e);
            }
            ret = ConfigFactory.create(ESyncConfig.class, props);
        } else {
            ret = ConfigFactory.create(ESyncConfig.class);
        }
        return ret;
    }

    private static void reportMetrics() {
        reporter.report();
        reporter.stop();
    }

    private static void runCheckers() {
        List<Runnable> checkers = new ArrayList<>();
        checkers.add(new AclChecker(config, eventBus));
        checkers.add(new CardinalityChecker(config, eventBus));
        checkers.add(new TypeCardinalityChecker(config, eventBus));
        ExecutorService pool = Executors.newFixedThreadPool(config
                .getPoolSize());
        for (Runnable checker : checkers) {
            pool.execute(checker);
        }
        pool.shutdown();
        try {
            if (pool.awaitTermination(config.getTimeoutMinutes(),
                    TimeUnit.MINUTES)) {
                log.info("All checkers terminated");
            } else {
                log.error(String.format(
                        "Timeout on worker pool after %d minutes.",
                        config.getTimeoutMinutes()));
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    private static void registerListener() {
        eventBus = new EventBus();
        eventBus.register(new InfoListener());
        eventBus.register(new TrailingListener());
        eventBus.register(new MissingListener());
        eventBus.register(new DiffListener());
        eventBus.register(new ErrorListener());
    }

}
