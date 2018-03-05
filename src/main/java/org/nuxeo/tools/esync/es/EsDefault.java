/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Benoit Delbosc
 */
package org.nuxeo.tools.esync.es;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Singleton;
import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.nuxeo.tools.esync.config.ESyncConfig;
import org.nuxeo.tools.esync.db.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;

@Singleton
public class EsDefault implements Es {
    private static final Logger log = LoggerFactory.getLogger(EsDefault.class);

    private static final String DOC_TYPE = "doc";

    private static final String ACL_FIELD = "ecm:acl";

    private static final String PATH_FIELD = "ecm:path";

    private static final String CHILDREN_FIELD = "ecm:path.children";
    private final static AtomicInteger clients = new AtomicInteger(0);
    private final static MetricRegistry registry = SharedMetricRegistries.getOrCreate("main");
    private final static Timer aclTimer = registry.timer("esync.es.acl");
    private final static Timer cardinalityTimer = registry.timer("esync.es.cardinality");
    private final static Timer typeCardinalityTimer = registry.timer("esync.es.type.cardinality");
    private static RestHighLevelClient client;
    private static RestClient lowLevelClient;
    private final Timer documentIdsForTypeTimed = registry.timer("esync.es.type.documentIdsForType");
    private ESyncConfig config;

    @Override
    public void initialize(ESyncConfig config) {
        this.config = config;
        open();
    }

    private void open() {
        synchronized (EsDefault.class) {
            if (clients.incrementAndGet() == 1) {
                log.debug("Connecting to ES cluster");

                String[] hosts = config.addressList().split(",");
                HttpHost[] httpHosts = new HttpHost[hosts.length];
                int i = 0;
                for (String host : hosts) {
                    httpHosts[i++] = HttpHost.create(host);
                }
                lowLevelClient = RestClient.builder(httpHosts).setRequestConfigCallback(
                        requestConfigBuilder -> requestConfigBuilder
                                .setConnectTimeout(config.connectTimeout())
                                .setSocketTimeout(config.socketTimeout()))
                        .setMaxRetryTimeoutMillis(config.maxRetryTimeout()).build();
                client = new RestHighLevelClient(lowLevelClient);
            }
        }
    }

    @Override
    public void close() {
        if (clients.decrementAndGet() == 0) {
            if (lowLevelClient != null) {
                log.debug("Closing es connection");
                try {
                    lowLevelClient.close();
                } catch (IOException e) {
                    log.error("Failed to close ES client", e);
                }
                lowLevelClient = null;
                client = null;
            }
        }
    }

    @Override
    public Document getDocument(String id) throws NoSuchElementException {

        try {
            GetRequest request = new GetRequest(config.esIndex(), DOC_TYPE, id).fetchSourceContext(
                    new FetchSourceContext(true, new String[]{ACL_FIELD, PATH_FIELD}, null));
            if (log.isDebugEnabled()) {
                log.debug(String.format("Get path of doc: curl -XGET 'http://localhost:9200/%s/%s/%s?fields=%s'",
                        config.esIndex(), DOC_TYPE, id, ACL_FIELD));
            }
            GetResponse response = getClient().get(request);
            if (!response.isExists()) {
                throw new NoSuchElementException(id + " not found in ES");
            }
            Map<String, Object> source = response.getSourceAsMap();
            Set<String> acl = getAclsFromSource(source);
            String path = source.get(PATH_FIELD).toString();
            return new Document(id, acl, path);
        } catch (IOException e) {
            log.error("Failed to get document ", e);
            return null;
        }
    }

    protected Set<String> getAclsFromSource(Map<String, Object> source) {
        Set<String> acl;
        try {
            Object aclsObj = source.get(ACL_FIELD);
            if (aclsObj instanceof List) {
                acl = new HashSet((List)aclsObj);
            } else  {
                log.warn("Unknown acl type "+aclsObj);
                acl = Document.NO_ACL;
            }
        } catch (NullPointerException e) {
            acl = Document.NO_ACL;
        }
        return acl;
    }

    @Override
    public List<Document> getDocsWithInvalidAcl(Set<String> acl, String path, List<String> excludePaths) {
        final Timer.Context context = aclTimer.time();
        try {
            return getDocsWithInvalidAclTimed(acl, path, excludePaths);
        } finally {
            context.stop();
        }
    }

    private List<Document> getDocsWithInvalidAclTimed(Set<String> acl, String path, List<String> excludePaths) {

        SearchRequest searchRequest = searchRequest();
        BoolQueryBuilder boolq = QueryBuilders.boolQuery();

        // Looking for a different ACL
        if (acl != null && acl.size() > 0) {
            boolq.mustNot(QueryBuilders.termQuery(ACL_FIELD, acl));
        } else {
            boolq.mustNot(QueryBuilders.existsQuery(ACL_FIELD));
        }
        // Starts with path
        boolq.filter(QueryBuilders.termQuery(CHILDREN_FIELD, path));
        if (!excludePaths.isEmpty()) {
            // Excluding paths
            for (String excludePath : excludePaths) {
                boolq.mustNot(QueryBuilders.termQuery(CHILDREN_FIELD, excludePath));
            }
        }

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .size(config.maxResults())
                .fetchSource(new FetchSourceContext(true, new String[]{ACL_FIELD, PATH_FIELD}, null))
                .query(boolq);
        searchRequest.source(sourceBuilder);

        logSearchRequest(searchRequest);
        try {
            SearchResponse response = getClient().search(searchRequest);
            logSearchResponse(response);
            long hits = response.getHits().getTotalHits();
            List<Document> ret = new ArrayList<>((int) hits);
            if (hits > 0) {
                log.info(String.format("%d docs with potential invalid ACL found on ES at %s", hits, path));
                for (SearchHit hit : response.getHits()) {
                    Map<String, Object> hitSource = hit.getSourceAsMap();
                    Set<String> aclSet = getAclsFromSource(hitSource);
                    Document esDoc = new Document(hit.getId(), aclSet, hitSource.get(PATH_FIELD).toString());
                    ret.add(esDoc);
                }
            }
            return ret;
        } catch (IOException e) {
            log.error("Failed to get Docs With Invalid Acl", e);
            return Collections.emptyList();
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

    /**
     * Count the results of a query
     */
    protected long count(QueryBuilder query) {
        SearchRequest searchRequest = searchRequest().source(new SearchSourceBuilder().size(0).query(query));
        logSearchRequest(searchRequest);
        try {
            SearchResponse response = getClient().search(searchRequest);
            logSearchResponse(response);
            return response.getHits().getTotalHits();
        } catch (IOException e) {
            log.error("Failed to count documents ", e);
            return -1;
        }
    }

    /**
     * Creates a search request
     */
    protected SearchRequest searchRequest() {
        return new SearchRequest(config.esIndex()).searchType(SearchType.DFS_QUERY_THEN_FETCH).types(DOC_TYPE);
    }

    @Override
    public long getProxyCardinality() {
        return count(QueryBuilders.termQuery("ecm:isProxy", "true"));
    }

    @Override
    public long getVersionCardinality() {
        return count(QueryBuilders.termQuery("ecm:isVersion", "true"));
    }

    @Override
    public long getOrphanCardinality() {
        return count(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("ecm:parentId")));
    }

    private long getCardinalityTimed() {
        return count(QueryBuilders.boolQuery()
                .mustNot(QueryBuilders.termQuery("ecm:isProxy", "true"))
                .mustNot(QueryBuilders.termQuery("ecm:isVersion", "true")));
    }

    @Override
    public Map<String, Long> getTypeCardinality() {
        final Timer.Context context = typeCardinalityTimer.time();
        try {
            return getTypeCardinalityTimed();
        } finally {
            context.stop();
        }
    }

    private Map<String, Long> getTypeCardinalityTimed() {

        Map<String, Long> ret = new LinkedHashMap();
        SearchRequest searchRequest = searchRequest();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .size(0)
                .query(QueryBuilders.boolQuery().mustNot(QueryBuilders.termQuery("ecm:isProxy", "true")))
                .aggregation(AggregationBuilders.terms("primaryType").field("ecm:primaryType"));

        searchRequest.source(sourceBuilder);
        logSearchRequest(searchRequest);
        try {
            SearchResponse response = getClient().search(searchRequest);
            logSearchResponse(response);

            Terms terms = response.getAggregations().get("primaryType");
            for (Terms.Bucket term : terms.getBuckets()) {
                ret.put(term.getKeyAsString(), term.getDocCount());
            }
        } catch (IOException e) {
            log.error("Failed to get Type Cardinality", e);
        }
        return ret;
    }

    @Override
    public Set<String> getDocumentIdsForType(String type) {
        final Timer.Context context = documentIdsForTypeTimed.time();
        try {
            return getDocumentIdsForTypeTimed(type);
        } finally {
            context.stop();
        }
    }

    private Set<String> getDocumentIdsForTypeTimed(String type) {

        Set<String> ret = new HashSet<>();
        SearchRequest searchRequest = searchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .size(config.getScrollSize()).query(QueryBuilders.boolQuery()
                        .filter(QueryBuilders.termQuery("ecm:primaryType", type))
                        .mustNot(QueryBuilders.termQuery("ecm:isProxy", "true")));

        searchRequest.scroll(getScrollTime()).source(searchSourceBuilder);
        logSearchRequest(searchRequest);
        try {
            SearchResponse searchResponse = client.search(searchRequest);
            String scrollId = searchResponse.getScrollId();
            SearchHits hits = searchResponse.getHits();

            while (hits.getHits().length > 0) {
                if (log.isTraceEnabled()) {
                    logSearchResponse(searchResponse);
                }
                for (SearchHit hit : hits.getHits()) {
                    ret.add(hit.getId());
                }
                if (ret.size() >= hits.getTotalHits()) {
                    break;
                }
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(getScrollTime());
                searchResponse = client.searchScroll(scrollRequest);
                scrollId = searchResponse.getScrollId();
                hits = searchResponse.getHits();
            }
        } catch (IOException e) {
            log.error("Failed to getDocumentIds For Type", e);
        }

        return ret;
    }

    private TimeValue getScrollTime() {
        return TimeValue.timeValueMinutes(config.getScrollTime());
    }

    private RestHighLevelClient getClient() {
        return client;
    }

    private void logSearchResponse(SearchResponse response) {
        if (log.isDebugEnabled()) {
            log.debug("Response: " + response.toString());
        }
    }

    private void logSearchRequest(SearchRequest request) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Search query: curl -XGET 'http://localhost:9200/%s/%s/_search?pretty' -d '%s'",
                    config.esIndex(), DOC_TYPE, request.toString()));
        }
    }

}
