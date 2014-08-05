package checker;

import com.google.common.eventbus.EventBus;

import config.ESyncConfig;
import db.Db;
import db.DbSql;
import es.Es;
import es.EsDefault;
import event.Event;
import event.InfoEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

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

}
