package com.digitalbridge.mongodb.data.mapping;

import org.springframework.data.mapping.PersistentProperty;

/**
 * Domain service to allow accessing and setting {@link org.springframework.data.mapping.PersistentProperty}s of an
 * entity. Usually obtained through {@link PersistentEntity#getPropertyAccessor(Object)}. In case type conversion shall
 * be applied on property access, use a {@link com.digitalbridge.mongodb.data.mapping.ConvertingPropertyAccessor}.
 *
 * @since 1.10
 * @see PersistentEntity#getPropertyAccessor(Object)
 * @see ConvertingPropertyAccessor
 * @version 1:0
 * @author rajakolli
 */
public interface PersistentPropertyAccessor {

  /**
   * Sets the given {@link org.springframework.data.mapping.PersistentProperty} to the given value. Will do type
   * conversion if a {@link org.springframework.core.convert.ConversionService} is configured.
   *
   * @param property must not be {@literal null}.
   * @param value can be {@literal null}.
   * @throws org.springframework.data.mapping.model.MappingException in case an exception occurred when setting the
   *           property value.
   */
  void setProperty(PersistentProperty<?> property, Object value);

  /**
   * Returns the value of the given {@link org.springframework.data.mapping.PersistentProperty} of the underlying bean
   * instance.
   *
   * @param property must not be {@literal null}.
   * @return can be {@literal null}.
   */
  Object getProperty(PersistentProperty<?> property);

  /**
   * Returns the underlying bean.
   *
   * @return will never be {@literal null}.
   */
  Object getBean();
}
