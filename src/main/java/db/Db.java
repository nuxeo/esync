package db;

import java.util.List;

import config.ESyncConfig;

public interface Db {

    void initialize(ESyncConfig config);

    List<Document> getDocumentWithAcl();

}
