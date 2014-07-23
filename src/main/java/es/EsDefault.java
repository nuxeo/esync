package es;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.ESyncConfig;
import db.Document;

public class EsDefault implements Es {
    private static final Logger log = LoggerFactory.getLogger(EsDefault.class);
    private static final String DOC_TYPE = "doc";
    private static final String ACL_FIELD = "ecm:acl";
    private static final String PATH_FIELD = "ecm:path";
    private static final String CHILDREN_FIELD = "ecm:path.children";
    private ESyncConfig config;
    private TransportClient client;

    @Override
    public void initialize(ESyncConfig config) {
        this.config = config;
    }

    @Override
    public Document getDocument(String id) throws NoSuchElementException {
        GetRequestBuilder getRequest = getClient().prepareGet(config.esIndex(),
                DOC_TYPE, id).setFields(ACL_FIELD, PATH_FIELD);
        if (log.isDebugEnabled()) {
            log.debug(String
                    .format("Get path of doc: curl -XGET 'http://localhost:9200/%s/%s/%s?fields=%s'",
                            config.esIndex(), DOC_TYPE, id, ACL_FIELD));
        }
        GetResponse ret = getRequest.execute().actionGet();
        if (!ret.isExists()) {
            throw new NoSuchElementException(id + " not found in ES");
        }
        String acl[];
        try {
            Object[] aclArray = ret.getField(ACL_FIELD).getValues().toArray();
            acl = Arrays.copyOf(aclArray, aclArray.length, String[].class);
        } catch (NullPointerException e) {
            acl = Document.NO_ACL;
        }
        String path = ret.getField(PATH_FIELD).getValue().toString();
        return new Document(id, acl, path);
    }

    @Override
    public void checkSameAcl(String[] acl, String path,
            List<String> excludePaths) {
        AndFilterBuilder filter = FilterBuilders.andFilter();
        // Looking for a different ACL
        if (Document.NO_ACL == acl) {
            filter.add(FilterBuilders.existsFilter(ACL_FIELD));
        } else {
            filter.add(FilterBuilders.notFilter(FilterBuilders.termFilter(
                    ACL_FIELD, acl)));
        }
        // Starts with path
        filter.add(FilterBuilders.termFilter(CHILDREN_FIELD, path));
        if (!excludePaths.isEmpty()) {
            // Excluding paths
            AndFilterBuilder excludeClause = FilterBuilders.andFilter();
            for (String excludePath : excludePaths) {
                excludeClause.add(FilterBuilders.termFilter(CHILDREN_FIELD,
                        excludePath));
            }
            filter.add(FilterBuilders.notFilter(excludeClause));
        }
        SearchRequestBuilder request = getClient()
                .prepareSearch(config.esIndex()).setTypes(DOC_TYPE)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setSize(config.maxResults()).addFields(ACL_FIELD, PATH_FIELD)
                .setQuery(QueryBuilders.constantScoreQuery(filter));
        logSearchRequest(request);
        SearchResponse response = request.execute().actionGet();
        logSearchResponse(response);
        long hits = response.getHits().getTotalHits();
        if (hits > 0) {
            log.error(String.format("%d invalid ACL found on ES", hits));
            for (SearchHit hit : response.getHits()) {
                log.info("invalid acl for "
                        + hit.field(PATH_FIELD).getValue().toString() + " "
                        + hit.field(ACL_FIELD).getValues().toString());
            }
        }
    }

    private Client getClient() {
        if (client == null) {
            log.info("Connecting to an ES cluster");
            ImmutableSettings.Builder builder = ImmutableSettings
                    .settingsBuilder()
                    .put("cluster.name", config.clusterName())
                    .put("client.transport.sniff", false);
            Settings settings = builder.build();
            log.debug("Using settings: " + settings.toDelimitedString(','));
            TransportClient tClient = new TransportClient(settings);
            String[] addresses = config.addressList().split(",");
            for (String item : addresses) {
                String[] address = item.split(":");
                log.info("Add transport address: " + item);
                try {
                    InetAddress inet = InetAddress.getByName(address[0]);
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

    private void logSearchResponse(SearchResponse response) {
        if (log.isDebugEnabled()) {
            log.debug("Response: " + response.toString());
        }
    }

    private void logSearchRequest(SearchRequestBuilder request) {
        if (log.isDebugEnabled()) {
            log.debug(String
                    .format("Search query: curl -XGET 'http://localhost:9200/%s/%s/_search?pretty' -d '%s'",
                            config.esIndex(), DOC_TYPE, request.toString()));
        }
    }

}
