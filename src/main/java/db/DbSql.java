package db;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import config.ESyncConfig;

public class DbSql implements Db {
    private static final Log log = LogFactory.getLog(DbSql.class);

    private static final String ACL_QUERY = "SELECT id, nx_get_read_acl(id) FROM (SELECT DISTINCT(id) FROM acls) AS foo";
    private static final String DENY_ALL = ",-Everyone";

    private ESyncConfig config;

    @Override
    public void initialize(ESyncConfig config) {
        this.config = config;
    }

    @Override
    public List<Document> getDocumentWithAcl() {
        Connection conn = getDbConnection();
        List<Document> ret =  null;
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(ACL_QUERY);
            ResultSet rs = ps.executeQuery();
            if (! rs.next()) {
                ret = Collections.emptyList();
            } else {
                ret = new ArrayList<Document>();
                do {
                    ret.add(new Document(rs.getString(1),
                            rs.getString(2).replace(DENY_ALL, "")));
                } while (rs.next());
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return ret;
    }

    private Connection getDbConnection() {
        log.info("Connect to database:" + config.dbUrl() + " from " + getHostName());
        try {
            Class.forName(config.dbDriver());
            return DriverManager.getConnection(config.dbUrl(), config.dbUser(), config.dbPassword());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException(e);
        } catch (ClassNotFoundException e) {
            String msg = "Missing JDBC Driver: " + e.getMessage();
            log.error(msg);
            throw new RuntimeException(msg);
        }
    }

    private static String getHostName() {
        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostname = "unknown";
        }
        return hostname;
    }

}
