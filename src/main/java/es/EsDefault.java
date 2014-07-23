package es;

import config.ESyncConfig;
import db.DbSql;
import db.Document;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.apache.commons.lang.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.security.acl.Acl;
import java.util.NoSuchElementException;

public class EsDefault implements Es {
    private static final Log log = LogFactory.getLog(EsDefault.class);
    private static final String DOC_TYPE = "doc";
    private static final String ACL_FIELD = "ecm:acl";
    private static final String PATH_FIELD = "ecm:path";
    private ESyncConfig config;
    private TransportClient client;

    @Override
    public void initialize(ESyncConfig config) {
        this.config = config;
    }

    @Override
    public Document getDocument(String id) throws NoSuchElementException {
        GetRequestBuilder getRequest = getClient()
                .prepareGet(config.esIndex(), DOC_TYPE, id)
                .setFields(ACL_FIELD, PATH_FIELD);
        if (log.isDebugEnabled()) {
            log.debug(String
                    .format("Get path of doc: curl -XGET 'http://localhost:9200/%s/%s/%s?fields=%s'",
                            config.esIndex(), DOC_TYPE, id, ACL_FIELD));
        }
        GetResponse ret = getRequest.execute().actionGet();
        if (!ret.isExists()) {
            throw new NoSuchElementException(id + " not found in ES");
        }
        String acl;
        try {
            acl = StringUtils.join(ret.getField(ACL_FIELD).getValues().toArray(), ",");
        } catch (NullPointerException e) {
            acl = null;
        }
        String path =ret.getField(PATH_FIELD).getValue().toString();
        return new Document(id, acl, path);
    }

    public Client getClient() {
        if (client == null) {
            log.info("Connecting to an ES cluster");
            ImmutableSettings.Builder builder = ImmutableSettings
                    .settingsBuilder()
                    .put("cluster.name", config.clusterName())
                    .put("client.transport.sniff", false);
            Settings settings = builder.build();
            log.debug("Using settings: "
                    + settings.toDelimitedString(','));
            TransportClient tClient = new TransportClient(settings);
            String[] addresses = config.addressList().split(",");
            for (String item : addresses) {
                String[] address = item.split(":");
                log.info("Add transport address: " + item);
                try {
                    InetAddress inet = InetAddress
                            .getByName(address[0]);
                    tClient.addTransportAddress(new InetSocketTransportAddress(
                            inet, Integer.parseInt(address[1])));
                } catch (UnknownHostException e) {
                    log.error("Unable to resolve host " + address[0], e);
                }
            }
            client = tClient;
        }
        return client;
    }
}
