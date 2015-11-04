package com.digitalbridge.mongodb.convert;

import org.springframework.data.mapping.model.SpELExpressionEvaluator;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;

import com.mongodb.DBObject;

/**
 * Internal API to trigger the resolution of properties.
 */
interface ValueResolver {

  /**
   * Resolves the value for the given {@link org.springframework.data.mongodb.core.mapping.MongoPersistentProperty}
   * within the given {@link com.mongodb.DBObject} using the given
   * {@link org.springframework.data.mapping.model.SpELExpressionEvaluator} and
   * {@link com.digitalbridge.mongodb.convert.ObjectPath}.
   *
   * @param prop a {@link org.springframework.data.mongodb.core.mapping.MongoPersistentProperty} object.
   * @param dbo a {@link com.mongodb.DBObject} object.
   * @param evaluator a {@link org.springframework.data.mapping.model.SpELExpressionEvaluator} object.
   * @param parent a {@link com.digitalbridge.mongodb.convert.ObjectPath} object.
   * @return a {@link java.lang.Object} object.
   */
  Object getValueInternal(MongoPersistentProperty prop, DBObject dbo, SpELExpressionEvaluator evaluator,
      ObjectPath parent);
}
