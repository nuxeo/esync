package es;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import config.ESyncConfig;
import db.Document;

public interface Es {

    void initialize(ESyncConfig config);

    void close();

    Document getDocument(String id) throws NoSuchElementException;

    List<Document> getDocsWithInvalidAcl(String[] acl, String path, List<String> excludePaths);

    long getCardinality();

    Map<String, Long> getTypeCardinality();

    Set<String> getDocumentIdsForType(String type);
}
