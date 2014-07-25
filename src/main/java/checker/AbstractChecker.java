package checker;

import com.google.common.eventbus.EventBus;

import config.ESyncConfig;
import db.Db;
import db.DbSql;
import es.Es;
import es.EsDefault;
import event.Event;
import event.InfoEvent;

public abstract class AbstractChecker implements Runnable {

    protected final ESyncConfig config;
    protected final EventBus eventBus;
    protected Db db;
    protected Es es;

    public AbstractChecker(ESyncConfig config, EventBus eventBus) {
        this.config = config;
        this.eventBus = eventBus;
    }

    public void post(Event event) {
        eventBus.post(event);
    }

    public void postMessage(String message) {
        eventBus.post(new InfoEvent(getName() + ": " + message));
    }

    @Override
    public void run() {
        postMessage("Starting");
        db = new DbSql();
        db.initialize(config);
        es = new EsDefault();
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
