package checker;

import javax.inject.Inject;

import com.google.common.eventbus.EventBus;

import config.ESyncConfig;
import db.Db;
import es.Es;
import event.ErrorEvent;
import event.Event;
import event.InfoEvent;

public abstract class AbstractChecker implements Runnable {

    @Inject
    ESyncConfig config;
    @Inject
    EventBus eventBus;
    @Inject
    Db db;
    @Inject
    Es es;

    public void post(Event event) {
        eventBus.post(event);
    }

    public void postMessage(String message) {
        eventBus.post(new InfoEvent(getName() + ": " + message));
    }

    public void postError(String message) {
        eventBus.post(new ErrorEvent(getName() + ": " + message));
    }

    public boolean autoRun() {
        return true;
    }

    @Override
    public void run() {
        postMessage("Starting");
        db.initialize(config);
        es.initialize(config);
        try {
            check();
        } finally {
            db.close();
            es.close();
        }
        postMessage("Terminated");
    }

    abstract void check();

    abstract String getName();

    public void init() {
    }
}
