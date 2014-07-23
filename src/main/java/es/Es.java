package es;

import java.util.NoSuchElementException;

import config.ESyncConfig;
import db.Document;

public interface Es {

    void initialize(ESyncConfig config);

    Document getDocument(String id) throws NoSuchElementException;

}
