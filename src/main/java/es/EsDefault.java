package es;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
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
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;

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
    private final static MetricRegistry registry = SharedMetricRegistries
            .getOrCreate("main");
    private final Timer aclTimer = registry.timer("esync.es.acl");
    private final Timer cardinalityTimer = registry
            .timer("esync.es.cardinality");
    private final Timer typeCardinalityTimer = registry
            .timer("esync.es.type.cardinality");

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
        final Timer.Context context = aclTimer.time();
        try {
            checkSameAclTimed(acl, path, excludePaths);
        } finally {
            context.stop();
        }
    }

    private void checkSameAclTimed(String[] acl, String path,
            List<String> excludePaths) {
        AndFilterBuilder filter = FilterBuilders.andFilter();
        // Looking for a different ACL
        if (Document.NO_ACL == acl) {
            filter.add(FilterBuilders.notFilter(FilterBuilders.missingFilter(
                    ACL_FIELD).nullValue(true)));
        } else {
            filter.add(FilterBuilders.notFilter(FilterBuilders.termFilter(
                    ACL_FIELD, acl)));
        }
        // Starts with path
        filter.add(FilterBuilders.termFilter(CHILDREN_FIELD, path));
        if (!excludePaths.isEmpty()) {
            // Excluding paths
            OrFilterBuilder excludeClause = FilterBuilders.orFilter();
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
                        + hit.field(ACL_FIELD).getValues().toString()
                        + " expecting " + StringUtils.join(acl, ","));
            }
        }
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
            client = null;
        }
    }

    @Override
    public long getCardinality() {
        final Timer.Context context = cardinalityTimer.time();
        try {
            return getCardinalityTimed();
        } finally {
            context.stop();
        }
    }

    @Override
    public Map<String, Integer> getTypeCardinality() {
        final Timer.Context context = typeCardinalityTimer.time();
        try {
            return getTypeCardinalityTimed();
        } finally {
            context.stop();
        }
    }

    private Map<String, Integer> getTypeCardinalityTimed() {
        SearchRequestBuilder request = getClient()
                .prepareSearch(config.esIndex()).setSearchType(SearchType.COUNT)
                .setQuery(QueryBuilders.matchAllQuery())
                .addAggregation(
                        AggregationBuilders.cardinality("primaryType").field(
                                "ecm:primaryType"));
        logSearchRequest(request);
        SearchResponse response = request.execute().actionGet();
        logSearchResponse(response);
        return null;

    }

    private long getCardinalityTimed() {
        CountRequestBuilder request = getClient()
                .prepareCount(config.esIndex()).setTypes(DOC_TYPE)
                .setQuery(QueryBuilders.matchAllQuery());
        logSearchRequest(request);
        CountResponse response = request.execute().actionGet();
        logSearchResponse(response);
        return response.getCount();
    }

    private Client getClient() {
        if (client == null) {
            log.debug("Connecting to an ES cluster");
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
                log.debug("Add transport address: " + item);
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

    private void logSearchResponse(ActionResponse response) {
        if (log.isDebugEnabled()) {
            log.debug("Response: " + response.toString());
        }
    }

    private void logSearchRequest(ActionRequestBuilder request) {
        if (log.isDebugEnabled()) {
            log.debug(String
                    .format("Search query: curl -XGET 'http://localhost:9200/%s/%s/_search?pretty' -d '%s'",
                            config.esIndex(), DOC_TYPE, request.toString()));
        }
    }

}
