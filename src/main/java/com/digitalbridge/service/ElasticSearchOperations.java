package com.digitalbridge.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.Base64;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.DateTimeZone;
import org.elasticsearch.common.joda.time.format.ISODateTimeFormat;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeFilterBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.range.date.DateRangeBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Order;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.digitalbridge.domain.AssetWrapper;
import com.digitalbridge.domain.FacetDateRange;
import com.digitalbridge.exception.DigitalBridgeException;
import com.digitalbridge.exception.DigitalBridgeExceptionBean;
import com.digitalbridge.mongodb.repository.AssetWrapperRepository;
import com.digitalbridge.util.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import io.searchbox.action.Action;
import io.searchbox.client.JestResult;
import io.searchbox.client.http.JestHttpClient;
import io.searchbox.core.Bulk;
import io.searchbox.core.Bulk.Builder;
import io.searchbox.core.Delete;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.SearchScroll;
import io.searchbox.core.search.aggregation.DateRangeAggregation;
import io.searchbox.core.search.aggregation.DateRangeAggregation.DateRange;
import io.searchbox.core.search.aggregation.TermsAggregation;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.Optimize;
import io.searchbox.indices.Refresh;
import io.searchbox.indices.Stats;
import io.searchbox.indices.mapping.PutMapping;
import io.searchbox.params.Parameters;
import io.searchbox.params.SearchType;

/**
 * <p>
 * ElasticSearchOperations class.
 * </p>
 *
 * @author rajakolli
 * @version 1: 0
 */
@RequestMapping(value = "/assetwrapper/search")
@RestController
public class ElasticSearchOperations {

  private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchOperations.class);

  private static final String INDEX_NAME = "digitalbridge";
  private static final String TYPE = "assetwrapper";

  private static final int PAGE_SIZE = 1000;

  private static final String TO_INDEX = "digitalbridge_alias";

  private static final int SIZE = Integer.MAX_VALUE;

  @Autowired JestHttpClient jestClient;

  @Autowired AssetWrapperRepository assetWrapperRepository;

  /**
   * <p>
   * performElasticSearch.
   * </p>
   *
   * @return a {@link org.springframework.data.domain.Page} object.
   * @throws com.digitalbridge.exception.DigitalBridgeException if any.
   */
  @Secured({ "ROLE_USER" })
  @RequestMapping(value = "/performElasticSearch", method = { RequestMethod.POST, RequestMethod.GET })
  public Page<AssetWrapper> performElasticSearch() throws DigitalBridgeException {
    return performElasticSearch(INDEX_NAME, TYPE);
  }

  /**
   * <p>performElasticSearch.</p>
   *
   * @param indexName a {@link java.lang.String} object.
   * @param typeName a {@link java.lang.String} object.
   * @return a {@link org.springframework.data.domain.Page} object.
   * @throws com.digitalbridge.exception.DigitalBridgeException if any.
   */
  protected Page<AssetWrapper> performElasticSearch(String indexName, String typeName) throws DigitalBridgeException {

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(QueryBuilders.multiMatchQuery("garden", "aName", "cuisine"));
    // searchSourceBuilder.explain(true);
    Search search = new Search.Builder(searchSourceBuilder.toString())
        // multiple index or types can be added.
        .addIndex(indexName).addType(typeName)
        // .addSort(new Sort("assetName", Sort.Sorting.DESC))
        .setHeader(getHeader())
        // .refresh(true)
        .build();
    // https://github.com/searchbox-io/Jest/tree/master/jest/src/test/java/io/searchbox/core
    List<String> assetIds = Collections.emptyList();
    try {
      SearchResult searchResult = jestClient.execute(search);
      if (searchResult.isSucceeded()) {
        JsonArray hits = searchResult.getJsonObject().getAsJsonObject("hits").getAsJsonArray("hits");
        assetIds = new ArrayList<String>();
        for (JsonElement jsonElement : hits) {
          assetIds.add(jsonElement.getAsJsonObject().get("_id").getAsString());
        }
      } else if (Constants.INDEXMISSINGCODE == searchResult.getResponseCode()) {
        LOGGER.error("IndexMissingException :{}", searchResult.getErrorMessage());
        DigitalBridgeExceptionBean bean = new DigitalBridgeExceptionBean();
        bean.setFaultCode("1001");
        bean.setFaultString(searchResult.getErrorMessage());
        throw new DigitalBridgeException(bean);
      }
    } catch (IOException e) {
      if ("java.net.ConnectException: Connection refused: connect".equalsIgnoreCase(e.getCause().toString())) {
        LOGGER.error("IOException occured while attempting to search {}", e.getMessage());
        DigitalBridgeExceptionBean bean = new DigitalBridgeExceptionBean();
        bean.setFaultCode("1004");
        bean.setFaultString("IOError");
        throw new DigitalBridgeException(bean);
        // TODO Send Email
      }
      if (e.getMessage().toString().contains("Read timed out")) {
        LOGGER.error("IOException occured while attempting to search {}", e.getMessage());
        DigitalBridgeExceptionBean bean = new DigitalBridgeExceptionBean();
        bean.setFaultCode("1005");
        bean.setFaultString("IOerror");
        throw new DigitalBridgeException(bean);
      }
    }

    Page<AssetWrapper> res = null;
    if (assetIds != null && !assetIds.isEmpty()) {
      res = assetWrapperRepository.findByIdIn(assetIds,
          new PageRequest(Constants.ZERO, Constants.THREE, Direction.ASC, "aName"));
    }

    return res;

  }

  /**
   * <p>
   * termFacetSearch.
   * </p>
   *
   * @return a {@link java.util.Map} object.
   * @throws com.digitalbridge.exception.DigitalBridgeException if any.
   * @param termFilters a {@link java.util.Map} object.
   * @param refresh a boolean.
   * @throws java.io.IOException if any.
   */
  @Secured({ "ROLE_USER" })
  @RequestMapping(value = "/termFacetSearch", method = { RequestMethod.POST, RequestMethod.GET })
  public Map<String, Map<String, Long>> termFacetSearch(Map<String, Object[]> termFilters, boolean refresh)
      throws DigitalBridgeException, IOException {

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery());
    AndFilterBuilder queryFilters = FilterBuilders.andFilter();
    if (!termFilters.isEmpty() && termFilters != null) {
      for (Entry<String, Object[]> termFilter : termFilters.entrySet()) {
        if (isKeyDateRangeKey(termFilter.getKey())) {
          FacetDateRange[] facetDateRange = convertObjectToFacetDateRange(termFilter.getValue());
          OrFilterBuilder orFilterBuilder = FilterBuilders.orFilter();
          for (int i = 0; i < facetDateRange.length; i++) {
            RangeFilterBuilder rangeFilterBuilder = new RangeFilterBuilder(termFilter.getKey());
            rangeFilterBuilder.gte(facetDateRange[i].getStartDate());
            rangeFilterBuilder.lte(facetDateRange[i].getEndDate());
            orFilterBuilder.add(rangeFilterBuilder);
          }
          queryFilters.add(orFilterBuilder);
        } else {
          queryFilters.add(FilterBuilders.termsFilter(termFilter.getKey(), termFilter.getValue()));
        }
      }
      // searchSourceBuilder.query(QueryBuilders.filteredQuery(queryBuilder, queryFilters));
      FilteredQueryBuilder filterQuery = new FilteredQueryBuilder(queryBuilder,
          FilterBuilders.boolFilter().must(queryFilters));
      searchSourceBuilder.query(filterQuery);
    } else {
      searchSourceBuilder.query(queryBuilder);
    }

    TermsBuilder cuisineTermsBuilder = AggregationBuilders.terms("MyCuisine").field("cuisine").size(SIZE)
        .order(Order.count(false));
    TermsBuilder boroughTermsBuilder = AggregationBuilders.terms("MyBorough").field("borough").size(SIZE)
        .order(Order.count(false));
    DateRangeBuilder dateRangeBuilder = AggregationBuilders.dateRange("MyDateRange").field("lDate");
    addDateRange(dateRangeBuilder);

    searchSourceBuilder.aggregation(cuisineTermsBuilder);
    searchSourceBuilder.aggregation(boroughTermsBuilder);
    searchSourceBuilder.aggregation(dateRangeBuilder);

    Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(INDEX_NAME).addType(TYPE)
        .setHeader(getHeader()).refresh(refresh).setSearchType(SearchType.DFS_QUERY_THEN_FETCH).build();

    SearchResult searchResult = null;
    Map<String, Map<String, Long>> resultMap = Collections.emptyMap();

    searchResult = (SearchResult) handleResult(search);
    if (searchResult.isSucceeded()) {
      TermsAggregation cuisineTerm = searchResult.getAggregations().getTermsAggregation("MyCuisine");
      Collection<io.searchbox.core.search.aggregation.TermsAggregation.Entry> cusineBuckets = cuisineTerm.getBuckets();
      Map<String, Long> cuisineMap = new LinkedHashMap<String, Long>(cusineBuckets.size());
      for (io.searchbox.core.search.aggregation.TermsAggregation.Entry bucket : cusineBuckets) {
        Long count = bucket.getCount();
        if (count > 0) {
          cuisineMap.put(bucket.getKey(), bucket.getCount());
        }
      }

      TermsAggregation boroughTerm = searchResult.getAggregations().getTermsAggregation("MyBorough");
      Collection<io.searchbox.core.search.aggregation.TermsAggregation.Entry> boroughBuckets = boroughTerm.getBuckets();
      Map<String, Long> boroughMap = new LinkedHashMap<String, Long>(boroughBuckets.size());
      for (io.searchbox.core.search.aggregation.TermsAggregation.Entry bucket : boroughBuckets) {
        long count = bucket.getCount();
        if (count > 0) {
          boroughMap.put(bucket.getKey(), bucket.getCount());
        }
      }

      DateRangeAggregation dateRangeTerm = searchResult.getAggregations().getDateRangeAggregation("MyDateRange");
      List<DateRange> dateRangeBuckets = dateRangeTerm.getBuckets();
      Map<String, Long> dateRangeMap = new LinkedHashMap<String, Long>(dateRangeBuckets.size());
      for (DateRange dateRange : dateRangeBuckets) {
        long count = dateRange.getCount();
        if (count > 0) {
          FacetDateRange facetDateRange = new FacetDateRange();
          facetDateRange.setStartDate(
              DateTime.parse(dateRange.getFromAsString(), ISODateTimeFormat.dateTimeParser().withOffsetParsed()));
          facetDateRange.setEndDate(
              DateTime.parse(dateRange.getToAsString(), ISODateTimeFormat.dateTimeParser().withOffsetParsed()));
          dateRangeMap.put(facetDateRange.toString(), dateRange.getCount());
        }
      }

      resultMap = new LinkedHashMap<String, Map<String, Long>>(Constants.THREE);
      if (MapUtils.isNotEmpty(cuisineMap)) {
        resultMap.put(cuisineTerm.getName(), cuisineMap);
      }
      if (MapUtils.isNotEmpty(boroughMap)) {
        resultMap.put(boroughTerm.getName(), boroughMap);
      }
      if (MapUtils.isNotEmpty(dateRangeMap)) {
        resultMap.put(dateRangeTerm.getName(), dateRangeMap);
      }
    }
    return resultMap;
  }

  /**
   * @param key
   * @return
   */
  private boolean isKeyDateRangeKey(String key) {
    if (Constants.DATEFIELDLIST.contains(key)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * @param value
   * @return
   */
  private FacetDateRange[] convertObjectToFacetDateRange(Object[] value) {
    FacetDateRange[] facetDateRange = new FacetDateRange[value.length];
    for (int i = 0; i < value.length; i++) {
      if (value[i] instanceof FacetDateRange) {
        facetDateRange[i] = (FacetDateRange) value[i];
      }
    }
    return facetDateRange;
  }

  /**
   * @param dateRangeBuilder
   */
  private void addDateRange(DateRangeBuilder dateRangeBuilder) {
    DateTime startMonthDate = new DateTime(DateTimeZone.UTC).withDayOfMonth(Constants.ONE).withTimeAtStartOfDay();
    dateRangeBuilder.addUnboundedTo(startMonthDate.minusMonths(Constants.TWELVE));
    for (int i = Constants.TWELVE; i > Constants.ZERO; i--) {
      dateRangeBuilder.addRange(startMonthDate.minusMonths(i),
          startMonthDate.minusMonths(i - 1).minusMillis(Constants.ONE));
    }
    dateRangeBuilder.addUnboundedFrom(startMonthDate);
  }

  /**
   * <p>
   * createGeoPointMapping.
   * </p>
   */
  @Secured({ "ROLE_ADMIN" })
  @RequestMapping(value = "/createGeoPointMapping")
  public void createGeoPointMapping() {
    String expectedMappingSource = "{\"mappings\":{\"assetwrapper\":{\"properties\":{\"address\":{\"properties\":{\"building\":{\"type\":\"string\"},\"location\":{\"type\":\"geo_point\"},\"street\":{\"type\":\"string\"},\"zipcode\":{\"type\":\"string\"}}},\"assetName\":{\"type\":\"string\"},\"borough\":{\"type\":\"string\"},\"cuisine\":{\"type\":\"string\"},\"notes\":{\"properties\":{\"date\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},\"note\":{\"type\":\"string\"},\"score\":{\"type\":\"long\"}}},\"orginalAssetId\":{\"type\":\"string\"}}}}}";
    PutMapping putMapping = new PutMapping.Builder(TO_INDEX, null, expectedMappingSource).setHeader(getHeader())
        .build();
    try {
      JestResult val = jestClient.execute(putMapping);
      LOGGER.info(val.isSucceeded() ? "created Index Successfully" : "Failed to create Index");
    } catch (JsonSyntaxException e) {
      LOGGER.error("JsonSyntaxException occured while attempting to create GeoPointMapping", e.getMessage());
    } catch (Exception e) {
      LOGGER.error("Exception occured while attempting to create GeoPointMapping", e.getMessage());
    }
  }
  
  /**
   * <p>createIndexes.</p>
   *
   * @param indexName a {@link java.lang.String} object.
   * @param type a {@link java.lang.String} object.
   * @return a {@link io.searchbox.client.JestResult} object.
   * @throws com.digitalbridge.exception.DigitalBridgeException if any.
   */
  @Secured({ "ROLE_ADMIN" })
  @RequestMapping(value = "/createIndexes/{indexName}/{type}")
  public JestResult createIndexes(@PathVariable("indexName") String indexName, @PathVariable("type") String type)
      throws DigitalBridgeException {
    ImmutableSettings.Builder settingsBuilder = ImmutableSettings.settingsBuilder();
    settingsBuilder.put("number_of_shards", 5);
    settingsBuilder.put("number_of_replicas", 1);
    CreateIndex indexBuilder = new CreateIndex.Builder(indexName.toLowerCase()).setHeader(getHeader())
        .settings(settingsBuilder.build().getAsMap()).build();
    return handleResult(indexBuilder);
  }

  /**
   * <p>
   * dropIndexes.
   * </p>
   *
   * @param indexName a {@link java.lang.String} object.
   * @throws com.digitalbridge.exception.DigitalBridgeException if any.
   * @param type a {@link java.lang.String} object.
   * @return a {@link io.searchbox.core.DocumentResult} object.
   */
  @Secured({ "ROLE_ADMIN" })
  @RequestMapping(value = "/dropIndexes/{indexName}/{type}")
  public DocumentResult dropIndexes(@PathVariable("indexName") String indexName, @PathVariable("type") String type)
      throws DigitalBridgeException {
    DocumentResult res = null;
    try {
      Delete deleteBuilder = null;
      if (StringUtils.isNotBlank(type)) {
        deleteBuilder = new Delete.Builder(type).index(indexName.toLowerCase()).setHeader(getHeader()).build();
      } else {
        deleteBuilder = new Delete.Builder(indexName.toLowerCase()).setHeader(getHeader()).build();
      }
      res = jestClient.execute(deleteBuilder);
      LOGGER.info(res.getJsonString());
    } catch (IOException e) {
      LOGGER.error("unable to delete Indexes ", e.getMessage());
      DigitalBridgeExceptionBean bean = new DigitalBridgeExceptionBean();
      bean.setFaultCode("1007");
      bean.setFaultString("Unable to Delete");
      throw new DigitalBridgeException(bean);
    }
    return res;
  }

  /**
   * <p>
   * optimizeIndex.
   * </p>
   *
   * @throws com.digitalbridge.exception.DigitalBridgeException if any.
   */
  @RequestMapping(value = "optimizeIndex")
  /*5000 = 5 seconds*/
  @Scheduled(fixedRate = 60000 * 60)
  public void optimizeIndex() throws DigitalBridgeException {
    Optimize optimize = new Optimize.Builder().maxNumSegments(1).setHeader(getHeader()).build();
    handleResult(optimize);
  }

  /**
   * <p>
   * refreshIndex.
   * </p>
   *
   * @param indexName a {@link java.lang.String} object.
   * @return a {@link io.searchbox.client.JestResult} object.
   * @throws com.digitalbridge.exception.DigitalBridgeException if any.
   */
  @Secured({ "ROLE_USER" })
  @RequestMapping(value = "refreshIndex/{indexName}")
  public JestResult refreshIndex(@PathVariable("indexName") String indexName) throws DigitalBridgeException {
    Refresh refresh = null;
    if (indexName != null && indexName.trim().length() > 0) {
      refresh = new Refresh.Builder().addIndex(indexName).setHeader(getHeader()).build();
    } else {
      refresh = new Refresh.Builder().setHeader(getHeader()).build();
    }
    return handleResult(refresh);
  }

  /**
   * <p>
   * elasticSearchStats.
   * </p>
   *
   * @return a {@link com.google.gson.JsonObject} object.
   * @throws com.digitalbridge.exception.DigitalBridgeException if any.
   */
  @Secured({ "ROLE_ADMIN" })
  @RequestMapping(value = "elasticSearchStats")
  public JsonObject elasticSearchStats() throws DigitalBridgeException {
    Stats stats = new Stats.Builder().setHeader(getHeader()).build();
    JsonObject statsJson = null;
    JestResult result = handleResult(stats);
    // confirm that response has all the default stats types
    JsonObject jsonResult = result.getJsonObject();
    statsJson = jsonResult.getAsJsonObject("indices").getAsJsonObject(INDEX_NAME).getAsJsonObject("total");
    Assert.notNull(statsJson);
    Assert.notNull(statsJson);
    Assert.notNull(statsJson.getAsJsonObject("docs"));
    Assert.notNull(statsJson.getAsJsonObject("store"));
    Assert.notNull(statsJson.getAsJsonObject("indexing"));
    Assert.notNull(statsJson.getAsJsonObject("get"));
    Assert.notNull(statsJson.getAsJsonObject("search"));
    return statsJson;
  }

  /**
   * <p>
   * reindex.
   * </p>
   *
   * @throws com.digitalbridge.exception.DigitalBridgeException if any.
   */
  @Secured({ "ROLE_ADMIN" })
  @RequestMapping(value = "reindex")
  public void reindex() throws DigitalBridgeException {
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(QueryBuilders.matchAllQuery());
    Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(INDEX_NAME).addType(TYPE)
        .setParameter(Parameters.SEARCH_TYPE, SearchType.SCAN).setParameter(Parameters.SIZE, PAGE_SIZE)
        .setParameter(Parameters.SCROLL, "5m").setHeader(getHeader()).build();
    LOGGER.debug(search.getData(null));
    JestResult result = handleResult(search);
    String scrollId = result.getJsonObject().get("_scroll_id").getAsString();

    int currentResultSize = 0;
    int pageNumber = 1;
    do {
      SearchScroll scroll = new SearchScroll.Builder(scrollId, "5m").setHeader(getHeader()).build();
      result = handleResult(scroll);
      scrollId = result.getJsonObject().get("_scroll_id").getAsString();
      JsonArray hits = result.getJsonObject().getAsJsonObject("hits").getAsJsonArray("hits");
      currentResultSize = hits.size();
      LOGGER.info("finished scrolling page # " + pageNumber++ + " which had " + currentResultSize + " results.");

      Builder bulkIndexBuilder = new Bulk.Builder().setHeader(getHeader()).defaultIndex(TO_INDEX).defaultType(TYPE);
      boolean somethingToIndex = false;
      for (int i = 0; i < currentResultSize; i++) {
        JsonObject hitValue = hits.get(i).getAsJsonObject();
        String sourceId = hitValue.get("_id").getAsString();
        JsonObject source = hitValue.get("_source").getAsJsonObject();

        // we are transforming the source, before adding it to the bulk
        // index queue
        for (Entry<String, JsonElement> sourceElement : source.entrySet()) {
          if (sourceElement.getKey().equals("address")) {
            JsonObject addressValues = sourceElement.getValue().getAsJsonObject();
            for (Entry<String, JsonElement> addressElement : addressValues.entrySet()) {
              if (addressElement.getKey().equalsIgnoreCase("location")) {
                String lat = null, lon = null;
                JsonObject locationValues = addressElement.getValue().getAsJsonObject();
                for (Entry<String, JsonElement> locationElement : locationValues.entrySet()) {
                  if (locationElement.getKey().equalsIgnoreCase("coordinates")) {
                    String loccoord = locationElement.getValue().toString().replace("[", "").replace("]", "");
                    lat = loccoord.split(",")[0].trim();
                    lon = loccoord.split(",")[1].trim();
                  }
                }
                locationValues.remove("type");
                locationValues.remove("coordinates");
                locationValues.addProperty("lat", lat);
                locationValues.addProperty("lon", lon);
              }
            }
            break;
          }
        }
        Index index = new Index.Builder(source).index(TO_INDEX).type(TYPE).id(sourceId).setHeader(getHeader()).build();
        bulkIndexBuilder = bulkIndexBuilder.addAction(index);
        somethingToIndex = true;
      }
      if (somethingToIndex) {
        Bulk bulk = bulkIndexBuilder.build();
        handleResult(bulk);
      } else {
        LOGGER.info("there weren't any results to index in this set/page");
      }
    } while (currentResultSize > PAGE_SIZE);
  }

  private Map<String, Object> getHeader() {
    Map<String, Object> map = new HashMap<String, Object>(1);
    map.put("Authorization", "Basic " + Base64.encodeBytes("admin:admin_pw".getBytes()));
    return map;
  }

  /**
   * <p>
   * handleResult.
   * </p>
   *
   * @param action a {@link io.searchbox.action.Action} object.
   * @return a {@link io.searchbox.client.JestResult} object.
   * @throws com.digitalbridge.exception.DigitalBridgeException if any.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected JestResult handleResult(Action action) throws DigitalBridgeException {
    JestResult jestResult = null;
    try {
      jestResult = jestClient.execute(action);
      if (!jestResult.isSucceeded()) {
        if (jestResult.getResponseCode() == Constants.CLUSTERBLOCKEXCEPTIONCODE) {
          LOGGER.error(jestResult.getErrorMessage());
          DigitalBridgeExceptionBean bean = new DigitalBridgeExceptionBean();
          bean.setFaultCode("1011");
          bean.setFaultString("ClusterBlockException");
          throw new DigitalBridgeException(bean);
        } else if (jestResult.getResponseCode() == Constants.INDEXMISSINGCODE) {
          LOGGER.error(jestResult.getErrorMessage());
          DigitalBridgeExceptionBean bean = new DigitalBridgeExceptionBean();
          bean.setFaultCode("1012");
          bean.setFaultString("IndexMissingException");
          throw new DigitalBridgeException(bean);
        } else {
          LOGGER.error(jestResult.getJsonString());
          LOGGER.error("Error :{}", jestResult.getErrorMessage());
        }
      }
    } catch (IOException e) {
      LOGGER.error("IOException occured while attempting to perform ElasticSearch Operation : {}", e.getMessage());
    }
    return jestResult;
  }

}
