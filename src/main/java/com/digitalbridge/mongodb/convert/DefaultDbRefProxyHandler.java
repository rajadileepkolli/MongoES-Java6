package com.digitalbridge.mongodb.convert;

import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.DefaultSpELExpressionEvaluator;
import org.springframework.data.mapping.model.SpELContext;
import org.springframework.data.mapping.model.SpELExpressionEvaluator;
import org.springframework.data.mongodb.core.convert.DbRefProxyHandler;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.util.Assert;

import com.digitalbridge.mongodb.data.mapping.BeanWrapper;
import com.digitalbridge.mongodb.data.mapping.PersistentPropertyAccessor;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

class DefaultDbRefProxyHandler implements DbRefProxyHandler {

  private final SpELContext spELContext;
  private final MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext;
  private final ValueResolver resolver;

  /**
   * <p>
   * Constructor for DefaultDbRefProxyHandler.
   * </p>
   *
   * @param spELContext must not be {@literal null}.
   * @param mappingContext must not be {@literal null}.
   * @param resolver a {@link com.digitalbridge.mongodb.convert.ValueResolver} object.
   */
  public DefaultDbRefProxyHandler(SpELContext spELContext,
      MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext,
      ValueResolver resolver) {

    this.spELContext = spELContext;
    this.mappingContext = mappingContext;
    this.resolver = resolver;
  }

  /* 
   * (non-Javadoc)
   * @see org.springframework.data.mongodb.core.convert.DbRefProxyHandler#populateId(com.mongodb.DBRef, java.lang.Object)
   */
  /** {@inheritDoc} */
  @Override
  public Object populateId(MongoPersistentProperty property, DBRef source, Object proxy) {

    if (source == null) {
      return proxy;
    }

    MongoPersistentEntity<?> entity = mappingContext.getPersistentEntity(property);
    MongoPersistentProperty idProperty = entity.getIdProperty();

    if (idProperty.usePropertyAccess()) {
      return proxy;
    }

    SpELExpressionEvaluator evaluator = new DefaultSpELExpressionEvaluator(proxy, spELContext);
    PersistentPropertyAccessor accessor = getPropertyAccessor(proxy);

    DBObject object = new BasicDBObject(idProperty.getFieldName(), source.getId());
    ObjectPath objectPath = ObjectPath.ROOT.push(proxy, entity, null);
    accessor.setProperty(idProperty, resolver.getValueInternal(idProperty, object, evaluator, objectPath));

    return proxy;
  }

  private PersistentPropertyAccessor getPropertyAccessor(Object bean) {
    Assert.notNull(bean, "Target bean must not be null!");
    return new BeanWrapper<Object>(bean);
  }
}
