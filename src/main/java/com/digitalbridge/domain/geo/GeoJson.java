package com.digitalbridge.domain.geo;

/**
 * <p>GeoJson interface.</p>
 *
 * @author rajakolli
 * @version 1: 0
 */
public interface GeoJson<T extends Iterable<?>> {

  /**
   * String value representing the type of the {@link com.demo.mongoes.model.GeoJson} object.
   *
   * @return will never be {@literal null}.
   * @see http://geojson.org/geojson-spec.html#geojson-objects
   */
  String getType();

  /**
   * The value of the coordinates member is always an {@link java.lang.Iterable}. The structure for the elements within
   * is determined by {@link #getType()} of geometry.
   *
   * @return will never be {@literal null}.
   * @see http://geojson.org/geojson-spec.html#geometry-objects
   */
  T getCoordinates();
}
