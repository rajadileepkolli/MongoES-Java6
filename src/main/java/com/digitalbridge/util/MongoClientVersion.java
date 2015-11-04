package com.digitalbridge.util;

import org.springframework.util.ClassUtils;

/**
 * {@link com.digitalbridge.util.MongoClientVersion} holds information about the used mongo-java client and is used to
 * distinguish between different versions.
 *
 * @version 1:0
 * @author rajakolli
 */
public class MongoClientVersion {

  private static final boolean IS_MONGO_30 = ClassUtils.isPresent("com.mongodb.binding.SingleServerBinding",
      MongoClientVersion.class.getClassLoader());
  private static final boolean IS_ASYNC_CLIENT = ClassUtils.isPresent("com.mongodb.async.client.MongoClient",
      MongoClientVersion.class.getClassLoader());

  private MongoClientVersion() {

  }

  /**
   * <p>
   * isMongo3Driver.
   * </p>
   *
   * @return |literal true} if MongoDB Java driver version 3.0 or later is on classpath.
   */
  public static boolean isMongo3Driver() {
    return IS_MONGO_30;
  }

  /**
   * <p>
   * isAsyncClient.
   * </p>
   *
   * @return {lliteral true} if MongoDB Java driver is on classpath.
   */
  public static boolean isAsyncClient() {
    return IS_ASYNC_CLIENT;
  }
}
