package db.dialect;

public class Mssql extends Dialect {
    private static final String ACL_QUERY = "SELECT id, primaryType, dbo.nx_get_read_acl(id) FROM hierarchy WHERE id IN (SELECT DISTINCT(id) FROM acls)";

    private static final String DOC_QUERY = "SELECT id, primaryType, dbo.nx_get_read_acl(id) FROM hierarchy";

    @Override
    public String getAclQuery() {
        return ACL_QUERY;
    }

    @Override
    public String getDocumentQuery(String id) {
        return String.format(DOC_QUERY, id);
    }
}
