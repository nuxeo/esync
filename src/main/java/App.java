import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import listener.DiffListener;
import listener.ErrorListener;
import listener.InfoListener;
import listener.MissingListener;
import listener.TrailingListener;

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

public class App implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    @Inject
    EventBus eventBus;
    @Inject
    ESyncConfig config;
    @Inject
    AclChecker aclChecker;
    @Inject
    TypeCardinalityChecker typeCardinalityChecker;
    @Inject
    CardinalityChecker cardinalityChecker;

    private final static MetricRegistry registry = SharedMetricRegistries
            .getOrCreate("main");
    private static ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private static PrintStream printStream = new PrintStream(baos);
    private final static ConsoleReporter reporter = ConsoleReporter
            .forRegistry(registry).outputTo(printStream)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS).build();

    @Override
    public void run() {
        log.info("Starting esync...");
        registerListener();
        runCheckers();
        log.info("End of esync");
        reportMetrics();
    }

    private static void reportMetrics() {
        reporter.report();
        reporter.stop();
        String stats = null;
        try {
            stats = baos.toString("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }
        log.debug(stats);
    }

    private void runCheckers() {
        List<Runnable> checkers = new ArrayList<>();
        checkers.add(aclChecker);
        checkers.add(cardinalityChecker);
        checkers.add(typeCardinalityChecker);
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

    private void registerListener() {
        eventBus.register(new InfoListener());
        eventBus.register(new TrailingListener());
        eventBus.register(new MissingListener());
        eventBus.register(new DiffListener());
        eventBus.register(new ErrorListener());
    }

}
