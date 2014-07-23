import java.io.IOException;
import java.sql.SQLException;

import checker.AclChecker;
import config.ESyncConfig;
import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Main {

    private static final Log log = LogFactory.getLog(Main.class);

    public static void main(String[] args) throws SQLException, IOException {
        log.info("Starting esync");
        ESyncConfig config = ConfigFactory.create(ESyncConfig.class);
        AclChecker aclChecker = new AclChecker(config);
        aclChecker.run();
        log.info("End of esync");
    }

}
