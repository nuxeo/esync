package config;

import org.aeonbits.owner.Config;

@Config.Sources({ "file:~/.esync.conf", "file:/etc/esync.conf" })
public interface ESyncConfig extends Config {

    @Key("nuxeo.db.user")
    String dbUser();

    @Key("nuxeo.db.password")
    String dbPassword();

    @Key("nuxeo.db.jdbc.url")
    String dbUrl();

    @Key("nuxeo.db.driver")
    String dbDriver();

    @Key("elasticsearch.indexName")
    String esIndex();

    @Key("elasticsearch.addressList")
    String addressList();

    @Key("elasticsearch.clusterName")
    String clusterName();

    @DefaultValue("1000")
    @Key("elasticsearch.maxResults")
    int maxResults();

    @DefaultValue("1")
    @Key("checker.pool.size")
    int getPoolSize();

    @DefaultValue("60")
    @Key("checker.pool.timeoutMinutes")
    long getTimeoutMinutes();
}
