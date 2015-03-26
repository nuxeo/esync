package db;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import config.ESyncConfig;

public interface Db {

    void initialize(ESyncConfig config);

    void close();

    /**
     * Return the list of document that holds an ACL
     */
    List<Document> getDocumentWithAcl();

    /**
     * Get the total number of documents, excluding proxies, versions and the Root document.
     */
    long getCardinality();

    long getProxyCardinality();

    long getVersionCardinality();

    long getOrphanCardinality();

    Document getDocument(String id);

    java.util.Map<String, Long> getTypeCardinality();

    Set<String> getDocumentIdsForType(String type);
}
