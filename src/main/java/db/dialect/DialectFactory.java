package db.dialect;

public class DialectFactory {

    private static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";
    private static final String MSSQL_DRIVE = "net.sourceforge.jtds.jdbc.Driver";
    private static final String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";

    public static Dialect create(String driver) {
        switch (driver) {
            case POSTGRESQL_DRIVER:
                return new Postgresql();
            case MSSQL_DRIVE:
                return new Mssql();
            case ORACLE_DRIVER:
                return new Oracle();
        }
        throw new IllegalArgumentException("Unknown driver :" + driver);
    }
}
