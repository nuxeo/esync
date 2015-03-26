import javax.inject.Singleton;

import checker.AclChecker;
import checker.CardinalityChecker;
import checker.DocumentTypeLister;
import checker.TypeCardinalityChecker;
import com.google.common.eventbus.EventBus;

import config.ESyncConfig;
import dagger.Module;
import dagger.Provides;
import db.Db;
import db.DbSql;
import es.Es;
import es.EsDefault;

/**
 * Dagger app module
 */
@Module(injects = { App.class,
        TypeCardinalityChecker.class,
        AclChecker.class,
        CardinalityChecker.class,
        DocumentTypeLister.class
        }, complete = false, library = true)
public class AppModule {

    private final ESyncConfig config;

    public AppModule(ESyncConfig config) {
        this.config = config;
    }

    @Provides
    ESyncConfig provideConfig() {
        return config;
    }

    @Provides
    @Singleton
    Es provideEs() {
        EsDefault ret = new EsDefault();
        return ret;
    }

    @Provides
    Db provideDb() {
        DbSql ret = new DbSql();
        // System.out.println("provide db " + ret);
        // for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
        //   System.out.println(ste);
        // }
        return ret;
    }

    @Provides
    @Singleton
    EventBus provideEventBus() {
        EventBus ret = new EventBus();
        return ret;
    }

}
