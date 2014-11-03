package config;

import org.aeonbits.owner.Config;

import java.util.List;

@Config.Sources({ "file:~/.esync.conf", "file:/etc/esync.conf" })
public interface ESyncConfig extends Config {

    @Key("nuxeo.db.user")
    String dbUser();

    @Key("nuxeo.db.password")
    String dbPassword();

    @Key("nuxeo.db.jdbc.url")
    String dbUrl();

    /**
     * The database driver class to load
     */
    @Key("nuxeo.db.driver")
    String dbDriver();

    /**
     * The name of the Elasticsearch index
     */
    @Key("elasticsearch.indexName")
    String esIndex();

    /**
     * A comma separated list of host:port to join the Elasticsearch cluster
     */
    @Key("elasticsearch.addressList")
    String addressList();

    /**
     * The name of the Elasticsearch cluster
     */
    @Key("elasticsearch.clusterName")
    String clusterName();

    /**
     * Limit the number of documents returned by Elasticsearch
     */
    @DefaultValue("1000")
    @Key("elasticsearch.maxResults")
    int maxResults();

    /**
     * Size of the pool of thread running the checkers
     */
    @DefaultValue("4")
    @Key("checker.pool.size")
    int getPoolSize();

    /**
     * Time to wait in minutes for checkers
     */
    @DefaultValue("60")
    @Key("checker.pool.timeoutMinutes")
    long getTimeoutMinutes();

    @Key("checker.list")
    List<String> getCheckers();
}
