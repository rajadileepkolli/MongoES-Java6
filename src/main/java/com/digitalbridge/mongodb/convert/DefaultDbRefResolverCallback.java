package com.digitalbridge.mongodb.convert;

import org.springframework.data.mapping.model.SpELExpressionEvaluator;
import org.springframework.data.mongodb.core.convert.DbRefResolverCallback;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;

import com.mongodb.DBObject;

/**
 * Default implementation of {@link DbRefResolverCallback}.
 */
class DefaultDbRefResolverCallback implements DbRefResolverCallback {

  private final DBObject surroundingObject;
  private final ObjectPath path;
  private final ValueResolver resolver;
  private final SpELExpressionEvaluator evaluator;

  /**
   * Creates a new {@link com.digitalbridge.mongodb.convert.DefaultDbRefResolverCallback} using the given
   * {@link com.mongodb.DBObject}, {@link com.digitalbridge.mongodb.convert.ObjectPath},
   * {@link com.digitalbridge.mongodb.convert.ValueResolver} and
   * {@link org.springframework.data.mapping.model.SpELExpressionEvaluator}.
   *
   * @param surroundingObject must not be {@literal null}.
   * @param path must not be {@literal null}.
   * @param evaluator must not be {@literal null}.
   * @param resolver a {@link com.digitalbridge.mongodb.convert.ValueResolver} object.
   */
  public DefaultDbRefResolverCallback(DBObject surroundingObject, ObjectPath path, SpELExpressionEvaluator evaluator,
      ValueResolver resolver) {

    this.surroundingObject = surroundingObject;
    this.path = path;
    this.resolver = resolver;
    this.evaluator = evaluator;
  }

  /* 
   * (non-Javadoc)
   * @see org.springframework.data.mongodb.core.convert.DbRefResolverCallback#resolve(org.springframework.data.mongodb.core.mapping.MongoPersistentProperty)
   */
  /** {@inheritDoc} */
  @Override
  public Object resolve(MongoPersistentProperty property) {
    return resolver.getValueInternal(property, surroundingObject, evaluator, path);
  }
}
