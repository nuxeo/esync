package db;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.ESyncConfig;

public class DbSql implements Db {
    private static final Logger log = LoggerFactory.getLogger(DbSql.class);

    private static final String ACL_QUERY = "SELECT id, nx_get_read_acl(id) FROM (SELECT DISTINCT(id) FROM acls) AS foo";
    private static final String COUNT_QUERY = "SELECT count(1) AS count FROM misc";
    private static final String TYPE_QUERY = "SELECT primarytype, count(1) AS count FROM hierarchy WHERE NOT isproperty GROUP BY primarytype ORDER BY 2 DESC";

    private static final String DENY_ALL = "-Everyone";
    private static final String UNSUPPORTED_ACL = "_UNSUPPORTED_ACL_";

    private ESyncConfig config;
    private Connection dbConnection;

    @Override
    public void initialize(ESyncConfig config) {
        this.config = config;
    }

    @Override
    public void close() {
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                log.warn(e.getMessage(), e);
            }
            dbConnection = null;
        }
    }

    @Override
    public List<Document> getDocumentWithAcl() {
        List<Document> ret;
        PreparedStatement ps;
        try {
            ps = getDbConnection().prepareStatement(ACL_QUERY);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                ret = Collections.emptyList();
            } else {
                ret = new ArrayList<>();
                do {
                    String[] acl = decodeAcl(rs.getString(2));
                    ret.add(new Document(rs.getString(1), acl));
                } while (rs.next());
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return ret;
    }

    @Override
    public long getTotalCountDocument() {
        int count = -1;
        try {
            Statement stmt = getDbConnection().createStatement();
            ResultSet ret = stmt.executeQuery(COUNT_QUERY);
            while(ret.next()) {
                count = ret.getInt("count");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return count;
    }

    private String[] decodeAcl(String aclString) {
        ArrayList<String> acl = new ArrayList<>(Arrays.asList(StringUtils
                .split(aclString, ",")));
        // remove trailing -Everyone
        int lastAceIndex = acl.size() - 1;
        if (DENY_ALL.equals(acl.get(lastAceIndex))) {
            acl.remove(lastAceIndex);
        }
        for (int i = 0; i < acl.size(); i++) {
            String ace = acl.get(i);
            if (ace.startsWith("-")) {
                acl.set(i, UNSUPPORTED_ACL);
            }
        }
        return acl.toArray(new String[acl.size()]);
    }

    private Connection getDbConnection() {
        if (dbConnection == null) {
            log.debug("Connect to database:" + config.dbUrl() + " from "
                    + getHostName());
            try {
                Class.forName(config.dbDriver());
                dbConnection = DriverManager.getConnection(config.dbUrl(), config.dbUser(),
                        config.dbPassword());
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new IllegalArgumentException(e);
            } catch (ClassNotFoundException e) {
                String msg = "Missing JDBC Driver: " + e.getMessage();
                log.error(msg);
                throw new RuntimeException(msg);
            }
        }
        return dbConnection;
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
