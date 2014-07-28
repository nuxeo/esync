package db.dialect;

public class DialectFactory {

    private static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";
    private static final String MSSQL_DRIVE = "net.sourceforge.jtds.jdbc.Driver";
    private static final String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";

    public static Dialect create(String driver) {
        if (POSTGRESQL_DRIVER.equals(driver)) {
            return new Postgresql();
        } else if (MSSQL_DRIVE.equals(driver)) {
            return new Mssql();
        } else if (ORACLE_DRIVER.equals(driver)) {
            return new Oracle();
        }
        throw new IllegalArgumentException("Unknown driver :" + driver);
    }
}
