package com.digitalbridge.domain.geo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Defines a {@link com.digitalbridge.domain.geo.GeoJsonGeometryCollection} that consists of a {@link java.util.List} of
 * {@link com.digitalbridge.domain.GeoJson} objects.
 *
 * @see http://geojson.org/geojson-spec.html#geometry-collection
 * @author rajakolli
 * @version 1:0
 */
public class GeoJsonGeometryCollection implements GeoJson<Iterable<GeoJson<?>>> {

  private static final String TYPE = "GeometryCollection";

  private final List<GeoJson<?>> geometries = new ArrayList<GeoJson<?>>();

  /**
   * Creates a new {@link com.digitalbridge.domain.geo.GeoJsonGeometryCollection} for the given
   * {@link com.digitalbridge.domain.GeoJson} instances.
   *
   * @param geometries a {@link java.util.List} object.
   */
  public GeoJsonGeometryCollection(List<GeoJson<?>> geometries) {

    Assert.notNull(geometries, "Geometries must not be null!");

    this.geometries.addAll(geometries);
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.mongodb.core.geo.GeoJson#getType()
   */
  /** {@inheritDoc} */
  @Override
  public String getType() {
    return TYPE;
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.mongodb.core.geo.GeoJson#getCoordinates()
   */
  /** {@inheritDoc} */
  @Override
  public Iterable<GeoJson<?>> getCoordinates() {
    return Collections.unmodifiableList(this.geometries);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return ObjectUtils.nullSafeHashCode(this.geometries);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }

    if (!(obj instanceof GeoJsonGeometryCollection)) {
      return false;
    }

    GeoJsonGeometryCollection other = (GeoJsonGeometryCollection) obj;

    return ObjectUtils.nullSafeEquals(this.geometries, other.geometries);
  }
}
