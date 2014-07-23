package db;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import config.ESyncConfig;
import org.slf4j.LoggerFactory;

public class DbSql implements Db {
    private static final Logger log = LoggerFactory.getLogger(DbSql.class);

    private static final String ACL_QUERY = "SELECT id, nx_get_read_acl(id) FROM (SELECT DISTINCT(id) FROM acls) AS foo";
    private static final String DENY_ALL = "-Everyone";
    private static final String UNSUPPORTED_ACL = "_UNSUPPORTED_ACL_";

    private ESyncConfig config;

    @Override
    public void initialize(ESyncConfig config) {
        this.config = config;
    }

    @Override
    public List<Document> getDocumentWithAcl() {
        Connection conn = getDbConnection();
        List<Document> ret;
        PreparedStatement ps;
        try {
            ps = conn.prepareStatement(ACL_QUERY);
            ResultSet rs = ps.executeQuery();
            if (! rs.next()) {
                ret = Collections.emptyList();
            } else {
                ret = new ArrayList<Document>();
                do {
                    String[] acl = decodeAcl(rs.getString(2));
                    ret.add(new Document(rs.getString(1), acl));
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

    private String[] decodeAcl(String aclString) throws SQLException {
        ArrayList<String> acl = new ArrayList<String>(Arrays.asList(StringUtils.split(aclString, ",")));
        // remove trailing -Everyone
        int lastAceIndex = acl.size() - 1;
        if (DENY_ALL.equals(acl.get(lastAceIndex))) {
            acl.remove(lastAceIndex);
        }
        for (int i=0; i<acl.size(); i++) {
            String ace = acl.get(i);
            if (ace.startsWith("-")) {
                acl.set(i, UNSUPPORTED_ACL);
            }
        }
        return acl.toArray(new String[acl.size()]);
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
