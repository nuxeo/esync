import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.ESyncConfig;
import dagger.ObjectGraph;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws SQLException, IOException {
        ESyncConfig config = getConfig(args);
        ObjectGraph objectGraph = ObjectGraph.create(new AppModule(config));
        App app = objectGraph.get(App.class);
        app.run();
    }

    private static ESyncConfig getConfig(String[] args) {
        ESyncConfig ret;
        if (args.length <= 0) {
            ret = ConfigFactory.create(ESyncConfig.class);
        } else {
            Properties props = new Properties();
            try {
                props.load(new FileInputStream(args[0]));
            } catch (IOException e) {
                log.error("Wrong file " + args[0] + e.getMessage(), e);
                throw new IllegalArgumentException(e);
            }
            ret = ConfigFactory.create(ESyncConfig.class, props);
        }
        return ret;
    }

}
