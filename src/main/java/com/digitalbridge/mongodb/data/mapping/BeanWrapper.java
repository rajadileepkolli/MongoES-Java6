package com.digitalbridge.mongodb.data.mapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Domain service to allow accessing the values of {@link PersistentProperty}s on a given bean.
 */
public class BeanWrapper<T> implements PersistentPropertyAccessor {

  private final T bean;

  /**
   * Creates a new {@link BeanWrapper} for the given bean.
   * 
   * @param bean must not be {@literal null}.
   */
  public BeanWrapper(T bean) {

    Assert.notNull(bean, "Bean must not be null!");
    this.bean = bean;
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.mapping.PersistentPropertyAccessor#setProperty(org.springframework.data.mapping.PersistentProperty, java.lang.Object)
   */
  public void setProperty(PersistentProperty<?> property, Object value) {

    Assert.notNull(property, "PersistentProperty must not be null!");

    try {

      if (!property.usePropertyAccess()) {

        ReflectionUtils.makeAccessible(property.getField());
        ReflectionUtils.setField(property.getField(), bean, value);
        return;
      }

      Method setter = property.getSetter();

      if (property.usePropertyAccess() && setter != null) {

        ReflectionUtils.makeAccessible(setter);
        ReflectionUtils.invokeMethod(setter, bean, value);
      }

    } catch (IllegalStateException e) {
      throw new MappingException("Could not set object property!", e);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.mapping.PersistentPropertyAccessor#getProperty(org.springframework.data.mapping.PersistentProperty)
   */
  public Object getProperty(PersistentProperty<?> property) {
    return getProperty(property, property.getType());
  }

  /**
   * Returns the value of the given {@link PersistentProperty} potentially converted to the given type.
   * 
   * @param <S>
   * @param property must not be {@literal null}.
   * @param type can be {@literal null}.
   * @return
   * @throws MappingException in case an exception occured when accessing the property.
   */
  @SuppressWarnings("unchecked")
  public <S> S getProperty(PersistentProperty<?> property, Class<? extends S> type) {

    Assert.notNull(property, "PersistentProperty must not be null!");

    try {

      if (!property.usePropertyAccess()) {

        Field field = property.getField();
        ReflectionUtils.makeAccessible(field);
        return (S) ReflectionUtils.getField(field, bean);
      }

      Method getter = property.getGetter();

      if (property.usePropertyAccess() && getter != null) {

        ReflectionUtils.makeAccessible(getter);
        return (S) ReflectionUtils.invokeMethod(getter, bean);
      }

      return null;

    } catch (IllegalStateException e) {
      throw new MappingException(
          String.format("Could not read property %s of %s!", property.toString(), bean.toString()), e);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.mapping.PersistentPropertyAccessor#getBean()
   */
  public T getBean() {
    return bean;
  }
}
