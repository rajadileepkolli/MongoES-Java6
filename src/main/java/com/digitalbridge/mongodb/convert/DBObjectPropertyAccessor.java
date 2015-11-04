package com.digitalbridge.mongodb.convert;

import java.util.Map;

import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

import com.mongodb.DBObject;

/**
 * {@link PropertyAccessor} to allow entity based field access to {@link DBObject}s.
 */
class DBObjectPropertyAccessor extends MapAccessor {

  static final MapAccessor INSTANCE = new DBObjectPropertyAccessor();

  /*
   * (non-Javadoc)
   * @see org.springframework.context.expression.MapAccessor#getSpecificTargetClasses()
   */
  /** {@inheritDoc} */
  @Override
  public Class<?>[] getSpecificTargetClasses() {
    return new Class[] { DBObject.class };
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.context.expression.MapAccessor#canRead(org.springframework.expression.EvaluationContext, java.lang.Object, java.lang.String)
   */
  /** {@inheritDoc} */
  @Override
  public boolean canRead(EvaluationContext context, Object target, String name) {
    return true;
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.context.expression.MapAccessor#read(org.springframework.expression.EvaluationContext, java.lang.Object, java.lang.String)
   */
  /** {@inheritDoc} */
  @Override
  @SuppressWarnings("unchecked")
  public TypedValue read(EvaluationContext context, Object target, String name) {

    Map<String, Object> source = (Map<String, Object>) target;

    Object value = source.get(name);
    return value == null ? TypedValue.NULL : new TypedValue(value);
  }
}
