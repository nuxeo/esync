package db.dialect;

public abstract class Dialect {
    private static final String COUNT_QUERY = "SELECT count(1) AS count FROM misc";
    private static final String TYPE_QUERY = "SELECT primarytype, count(1) AS count FROM hierarchy WHERE NOT isproperty GROUP BY primarytype ORDER BY 2 DESC";

    public abstract String getAclQuery();

    public String getCountQuery() {
        return COUNT_QUERY;
    }

    public String getTypeQuery() {
        return TYPE_QUERY;
    }
}
