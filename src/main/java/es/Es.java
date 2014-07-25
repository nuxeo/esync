package es;

import java.util.List;
import java.util.NoSuchElementException;

import config.ESyncConfig;
import db.Document;

public interface Es {

    void initialize(ESyncConfig config);

    void close();

    Document getDocument(String id) throws NoSuchElementException;

    void checkSameAcl(String[] acl, String path, List<String> excludePath);

    long getTotalCountDocument();
}
