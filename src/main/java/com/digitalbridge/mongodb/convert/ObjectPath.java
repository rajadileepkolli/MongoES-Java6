package com.digitalbridge.mongodb.convert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.mongodb.DBObject;

/**
 * A path of objects nested into each other. The type allows access to all parent objects currently in creation even
 * when resolving more nested objects. This allows to avoid re-resolving object instances that are logically equivalent
 * to already resolved ones.
 * <p>
 * An immutable ordered set of target objects for {@link DBObject} to {@link Object} conversions. Object paths can be
 * constructed by the {@link #toObjectPath(Object)} method and extended via {@link #push(Object)}.
 * 
 * @since 1.6
 */
public class ObjectPath {

  /** Constant <code>ROOT</code> */
  public static final ObjectPath ROOT = new ObjectPath();

  private final List<ObjectPathItem> items;

  private ObjectPath() {
    this.items = Collections.emptyList();
  }

  /**
   * Creates a new {@link ObjectPath} from the given parent {@link ObjectPath} by adding the provided
   * {@link ObjectPathItem} to it.
   * 
   * @param parent can be {@literal null}.
   * @param item
   */
  private ObjectPath(ObjectPath parent, ObjectPath.ObjectPathItem item) {

    List<ObjectPath.ObjectPathItem> items = new ArrayList<ObjectPath.ObjectPathItem>(parent.items);
    items.add(item);

    this.items = Collections.unmodifiableList(items);
  }

  /**
   * Returns a copy of the {@link com.digitalbridge.mongodb.convert.ObjectPath} with the given {@link java.lang.Object}
   * as current object.
   *
   * @param object must not be {@literal null}.
   * @param entity must not be {@literal null}.
   * @param id must not be {@literal null}.
   * @return a {@link com.digitalbridge.mongodb.convert.ObjectPath} object.
   */
  public ObjectPath push(Object object, MongoPersistentEntity<?> entity, Object id) {

    Assert.notNull(object, "Object must not be null!");
    Assert.notNull(entity, "MongoPersistentEntity must not be null!");

    ObjectPathItem item = new ObjectPathItem(object, id, entity.getCollection());
    return new ObjectPath(this, item);
  }

  /**
   * Returns the object with the given id and stored in the given collection if it's contained in the
   * {@link com.digitalbridge.mongodb.convert.ObjectPath} .
   *
   * @param id must not be {@literal null}.
   * @param collection must not be {@literal null} or empty.
   * @return a {@link java.lang.Object} object.
   */
  public Object getPathItem(Object id, String collection) {

    Assert.notNull(id, "Id must not be null!");
    Assert.hasText(collection, "Collection name must not be null!");

    for (ObjectPathItem item : items) {

      Object object = item.getObject();

      if (object == null) {
        continue;
      }

      if (item.getIdValue() == null) {
        continue;
      }

      if (collection.equals(item.getCollection()) && id.equals(item.getIdValue())) {
        return object;
      }
    }

    return null;
  }

  /**
   * Returns the current object of the {@link com.digitalbridge.mongodb.convert.ObjectPath} or {@literal null} if the
   * path is empty.
   *
   * @return a {@link java.lang.Object} object.
   */
  public Object getCurrentObject() {
    return items.isEmpty() ? null : items.get(items.size() - 1).getObject();
  }

  /* 
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  /** {@inheritDoc} */
  @Override
  public String toString() {

    if (items.isEmpty()) {
      return "[empty]";
    }

    List<String> strings = new ArrayList<String>(items.size());

    for (ObjectPathItem item : items) {
      strings.add(item.object.toString());
    }

    return StringUtils.collectionToDelimitedString(strings, " -> ");
  }

  /**
   * An item in an {@link ObjectPath}.
   */
  private static class ObjectPathItem {

    private final Object object;
    private final Object idValue;
    private final String collection;

    /**
     * Creates a new {@link ObjectPathItem}.
     * 
     * @param object
     * @param idValue
     * @param collection
     */
    ObjectPathItem(Object object, Object idValue, String collection) {

      this.object = object;
      this.idValue = idValue;
      this.collection = collection;
    }

    public Object getObject() {
      return object;
    }

    public Object getIdValue() {
      return idValue;
    }

    public String getCollection() {
      return collection;
    }
  }
}
