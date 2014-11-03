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

import checker.Discovery;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.google.common.eventbus.EventBus;

import config.ESyncConfig;
import dagger.ObjectGraph;

public class App implements Runnable {
    private final static Logger log = LoggerFactory.getLogger(App.class);

    private final static MetricRegistry registry = SharedMetricRegistries
            .getOrCreate("main");
    private final static ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private final static PrintStream printStream = new PrintStream(baos);
    private final static ConsoleReporter reporter = ConsoleReporter
            .forRegistry(registry).outputTo(printStream)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS).build();

    @Inject
    EventBus eventBus;
    @Inject
    ESyncConfig config;
    final List<Runnable> checkers = new ArrayList<>();

    public void injectCheckers(ObjectGraph objectGraph) {
        List<String> filter = config.getCheckers();
        for (Class checkerClass : Discovery.getCheckersClass(filter)) {
            log.info("Injecting checker: " + checkerClass.getSimpleName());
            checkers.add((Runnable) objectGraph.get(checkerClass));
        }
    }

    @Override
    public void run() {
        log.info("Starting esync...");
        registerListeners();
        runCheckers();
        log.info("End of esync");
        reportMetrics();
    }

    private void registerListeners() {
        eventBus.register(new InfoListener());
        eventBus.register(new TrailingListener());
        eventBus.register(new MissingListener());
        eventBus.register(new DiffListener());
        eventBus.register(new ErrorListener());
    }

    private void runCheckers() {
        int poolSize = Math.min(config.getPoolSize(), checkers.size());
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        log.info(String.format(
                "Executing %d checkers with a pool of %d thread(s).",
                checkers.size(), poolSize));
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
}
