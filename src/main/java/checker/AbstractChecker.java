package checker;

import config.ESyncConfig;
import db.Db;
import db.DbSql;
import es.Es;
import es.EsDefault;

/**
 * @since 5.9.2
 */
public abstract class AbstractChecker implements Runnable {

    protected final ESyncConfig config;
    protected Db db;
    protected Es es;

    public AbstractChecker(ESyncConfig config) {
        this.config = config;
    }

    @Override
    public void run() {
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

    }

    abstract void check();


}
