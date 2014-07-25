import java.io.IOException;
import java.sql.SQLException;

import checker.CountChecker;

import org.aeonbits.owner.ConfigFactory;


import checker.AclChecker;
import config.ESyncConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws SQLException, IOException {
        log.info("Starting esync");
        ESyncConfig config = ConfigFactory.create(ESyncConfig.class);

        AclChecker aclChecker = new AclChecker(config);
        aclChecker.run();

        CountChecker checker = new CountChecker(config);
        checker.run();

        log.info("End of esync");
    }

}
