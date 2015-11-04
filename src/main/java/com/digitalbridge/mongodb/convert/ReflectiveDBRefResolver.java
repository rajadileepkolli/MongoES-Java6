package com.digitalbridge.mongodb.convert;

import static org.springframework.util.ReflectionUtils.findMethod;
import static org.springframework.util.ReflectionUtils.invokeMethod;

import java.lang.reflect.Method;

import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.util.Assert;

import com.digitalbridge.util.MongoClientVersion;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

/**
 * {@link com.digitalbridge.mongodb.convert.ReflectiveDBRefResolver} provides reflective access to
 * {@link com.mongodb.DBRef} API that is not consistently available for various driver versions.
 *
 * @version 1:0
 * @author rajakolli
 */
class ReflectiveDBRefResolver {

  private static final Method FETCH_METHOD;

  static {
    FETCH_METHOD = findMethod(DBRef.class, "fetch");
  }

  private ReflectiveDBRefResolver() {

  }

  /**
   * Fetches the object referenced from the database either be directly calling {@link com.mongodb.DBRef#fetch()} or
   * {@link com.mongodb.DBCollection#findOne(Object)}.
   *
   * @param ref must not be {@literal null}.
   * @return the document that this references.
   * @param factory a {@link org.springframework.data.mongodb.MongoDbFactory} object.
   */
  public static DBObject fetch(MongoDbFactory factory, DBRef ref) {

    Assert.notNull(ref, "DBRef to fetch must not be null!");

    if (MongoClientVersion.isMongo3Driver()) {

      Assert.notNull(factory, "DbFactory to fetch DB from must not be null!");
      return factory.getDb().getCollection(ref.getCollectionName()).findOne(ref.getId());
    }

    return (DBObject) invokeMethod(FETCH_METHOD, ref);
  }

}
