import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import checker.CountChecker;

import org.aeonbits.owner.ConfigFactory;


import checker.AclChecker;
import config.ESyncConfig;

public class Main {
    static final Logger log = Logger.getLogger(Main.class);

    public static void main(String[] args) throws SQLException, IOException {
        BasicConfigurator.configure();
        log.info("Starting esync");
        ESyncConfig config = ConfigFactory.create(ESyncConfig.class);

        AclChecker aclChecker = new AclChecker(config);
        aclChecker.run();

        CountChecker checker = new CountChecker(config);
        checker.run();

        log.info("End of esync");
    }

}
