package db.dialect;

public abstract class Dialect {
    private static final String COUNT_QUERY = "SELECT (count(1) + 1) AS count FROM misc m JOIN hierarchy h ON m.id = h.id WHERE h.parentid IS NOT NULL";
    private static final String TYPE_QUERY = "SELECT primarytype, count(1) AS count FROM hierarchy h JOIN misc m ON m.id = h.id WHERE h.parentid IS NOT NULL GROUP BY primarytype ORDER BY 2 DESC";
    private static final String DOCUMENT_IDS_FOR_TYPE = "SELECT id FROM hierarchy WHERE primarytype = '%s' AND parentid IS NOT NULL";

    public abstract String getAclQuery();

    public String getCountQuery() {
        return COUNT_QUERY;
    }

    public String getTypeQuery() {
        return TYPE_QUERY;
    }

    public String getDocumentIdsForType(String type) {
        return String.format(DOCUMENT_IDS_FOR_TYPE, type);
    }
}
