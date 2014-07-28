package db.dialect;

public class Postgresql extends Dialect {
    private static final String ACL_QUERY = "SELECT id, nx_get_read_acl(id) FROM (SELECT DISTINCT(id) FROM acls) AS foo";

    @Override
    public String getAclQuery() {
        return ACL_QUERY;
    }
}
