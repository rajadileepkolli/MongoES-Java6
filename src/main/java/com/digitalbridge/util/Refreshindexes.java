package com.digitalbridge.util;

import java.io.IOException;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.searchbox.client.http.JestHttpClient;
import io.searchbox.core.Delete;
import io.searchbox.core.DocumentResult;

/**
 * <p>
 * Refreshindexes class.
 * </p>
 *
 * @author rajakolli
 * @version 1:0
 */
@RequestMapping(value = "/assetwrapper/search")
@RestController
public class Refreshindexes {
  private static final Logger LOGGER = LoggerFactory.getLogger(Refreshindexes.class);

  @Autowired JestHttpClient jestClient;

  /**
   * <p>
   * dropIndexes.
   * </p>
   *
   * @param indexName a {@link java.lang.String} object.
   */
  @RequestMapping(value = "/refreshIndexes/{indexName}")
  public void dropIndexes(@PathVariable("indexName") @NotNull String indexName) {
    try {
      DocumentResult res = jestClient.execute(new Delete.Builder("_all").index(indexName).build());
      LOGGER.info(res.getJsonString());
      mongoTemplate(indexName);
    } catch (IOException e) {
      LOGGER.error("unable to delete Indexes ", e.getMessage());
    }
  }

  private void mongoTemplate(String indexName) {
    /*	List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
    	credentialsList.add(MongoCredential.createCredential("deloitteAdmin", indexName, "password".toCharArray()));
    	ServerAddress addr = new ServerAddress(new InetSocketAddress("0.0.0.0", 27017));
    	MongoClient mongoClient = new MongoClient(addr, credentialsList);
    	MongoDatabase db = mongoClient.getDatabase(indexName);
    	MongoCollection<Document> myCollection = db.getCollection("assetwrapper");
    	try {
    		myCollection.drop();
    	} catch(MongoClientException mce) {
    		logger.error("{}",mce.getMessage());
    	}
    	finally {
    		mongoClient.close();
    	}*/
  }
}
