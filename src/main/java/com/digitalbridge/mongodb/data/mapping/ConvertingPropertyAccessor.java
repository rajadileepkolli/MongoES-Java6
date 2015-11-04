package com.digitalbridge.mongodb.data.mapping;

import org.springframework.core.convert.ConversionService;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.util.Assert;

/**
 * {@link com.digitalbridge.mongodb.data.mapping.PersistentPropertyAccessor} that potentially converts the value handed
 * to {@link #setProperty(PersistentProperty, Object)} to the type of the
 * {@link org.springframework.data.mapping.PersistentProperty} using a
 * {@link org.springframework.core.convert.ConversionService}. Exposes {@link #getProperty(PersistentProperty, Class)}
 * to allow obtaining the value of a property in a type the {@link org.springframework.core.convert.ConversionService}
 * can convert the raw type to.
 *
 * @version 1:0
 * @author rajakolli
 */
public class ConvertingPropertyAccessor implements PersistentPropertyAccessor {

  private final PersistentPropertyAccessor accessor;
  private final ConversionService conversionService;

  /**
   * Creates a new {@link com.digitalbridge.mongodb.data.mapping.ConvertingPropertyAccessor} for the given delegate
   * {@link com.digitalbridge.mongodb.data.mapping.PersistentPropertyAccessor} and
   * {@link org.springframework.core.convert.ConversionService}.
   *
   * @param accessor must not be {@literal null}.
   * @param conversionService must not be {@literal null}.
   */
  public ConvertingPropertyAccessor(PersistentPropertyAccessor accessor, ConversionService conversionService) {

    Assert.notNull(accessor, "PersistentPropertyAccessor must not be null!");
    Assert.notNull(conversionService, "ConversionService must not be null!");

    this.accessor = accessor;
    this.conversionService = conversionService;
  }

  /* 
   * (non-Javadoc)
   * @see org.springframework.data.mapping.PersistentPropertyAccessor#setProperty(org.springframework.data.mapping.PersistentProperty, java.lang.Object)
   */
  /** {@inheritDoc} */
  @Override
  public void setProperty(PersistentProperty<?> property, Object value) {
    accessor.setProperty(property, convertIfNecessary(value, property.getType()));
  }

  /* 
   * (non-Javadoc)
   * @see org.springframework.data.mapping.PersistentPropertyAccessor#getProperty(org.springframework.data.mapping.PersistentProperty)
   */
  /** {@inheritDoc} */
  @Override
  public Object getProperty(PersistentProperty<?> property) {
    return accessor.getProperty(property);
  }

  /**
   * Returns the value of the given {@link org.springframework.data.mapping.PersistentProperty} converted to the given
   * type.
   *
   * @param property must not be {@literal null}.
   * @param targetType must not be {@literal null}.
   * @param <T> a T object.
   * @return a T object.
   */
  public <T> T getProperty(PersistentProperty<?> property, Class<T> targetType) {

    Assert.notNull(property, "PersistentProperty must not be null!");
    Assert.notNull(targetType, "Target type must not be null!");

    return convertIfNecessary(getProperty(property), targetType);
  }

  /* 
   * (non-Javadoc)
   * @see org.springframework.data.mapping.PersistentPropertyAccessor#getBean()
   */
  /** {@inheritDoc} */
  @Override
  public Object getBean() {
    return accessor.getBean();
  }

  /**
   * Triggers the conversion of the source value into the target type unless the value already is a value of given
   * target type.
   * 
   * @param source can be {@literal null}.
   * @param type must not be {@literal null}.
   * @return
   */
  @SuppressWarnings("unchecked")
  private <T> T convertIfNecessary(Object source, Class<T> type) {
    return (T) (source == null ? source
        : type.isAssignableFrom(source.getClass()) ? source : conversionService.convert(source, type));
  }
}
