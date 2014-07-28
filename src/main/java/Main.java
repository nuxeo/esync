import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import listener.DiffListener;
import listener.InfoListener;
import listener.MissingListener;
import listener.TrailingListener;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import checker.AclChecker;
import checker.CountChecker;

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

    public static void main(String[] args) throws SQLException, IOException {
        log.info("Starting esync...");
        registerListener();
        runCheckers();
        log.info("End of esync");
        reportMetrics();
    }

    private static void reportMetrics() {
        reporter.report();
        reporter.stop();
    }

    private static void runCheckers() {
        ESyncConfig config = ConfigFactory.create(ESyncConfig.class);
        List<Runnable> checkers = new ArrayList<>();
        checkers.add(new AclChecker(config, eventBus));
        checkers.add(new CountChecker(config, eventBus));
        ExecutorService pool = Executors.newFixedThreadPool(config
                .getPoolSize());
        for (Runnable checker : checkers) {
            pool.execute(checker);
        }
        pool.shutdown();
        try {
            pool.awaitTermination(1000, TimeUnit.SECONDS);
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
    }

}
