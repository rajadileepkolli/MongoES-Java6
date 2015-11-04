package com.digitalbridge.domain.geo;

import java.util.List;

import org.springframework.data.geo.Point;

/**
 * {@link com.digitalbridge.domain.geo.GeoJsonLineString} is defined as list of at least 2
 * {@link org.springframework.data.geo.Point}s.
 *
 * @see http://geojson.org/geojson-spec.html#linestring
 * @author rajakolli
 * @version 1:0
 */
public class GeoJsonLineString extends GeoJsonMultiPoint {

  private static final String TYPE = "LineString";

  /**
   * Creates a new {@link com.digitalbridge.domain.geo.GeoJsonLineString} for the given
   * {@link org.springframework.data.geo.Point}s.
   *
   * @param points must not be {@literal null} and have at least 2 entries.
   */
  public GeoJsonLineString(List<Point> points) {
    super(points);
  }

  /**
   * Creates a new {@link com.digitalbridge.domain.geo.GeoJsonLineString} for the given
   * {@link org.springframework.data.geo.Point}s.
   *
   * @param first must not be {@literal null}
   * @param second must not be {@literal null}
   * @param others can be {@literal null}
   */
  public GeoJsonLineString(Point first, Point second, Point... others) {
    super(first, second, others);
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.mongodb.core.geo.GeoJsonMultiPoint#getType()
   */
  /** {@inheritDoc} */
  @Override
  public String getType() {
    return TYPE;
  }
}
