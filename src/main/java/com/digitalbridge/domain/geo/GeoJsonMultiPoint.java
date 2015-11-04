package com.digitalbridge.domain.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.data.geo.Point;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.digitalbridge.util.Constants;

/**
 * {@link com.digitalbridge.domain.geo.GeoJsonMultiPoint} is defined as list of
 * {@link org.springframework.data.geo.Point}s.
 *
 * @see http://geojson.org/geojson-spec.html#multipoint
 * @author rajakolli
 * @version 1:0
 */
public class GeoJsonMultiPoint implements GeoJson<Iterable<Point>> {

  private static final String TYPE = "MultiPoint";

  private final List<Point> points;

  /**
   * Creates a new {@link com.digitalbridge.domain.geo.GeoJsonMultiPoint} for the given
   * {@link org.springframework.data.geo.Point}s.
   *
   * @param points points must not be {@literal null} and have at least 2 entries.
   */
  public GeoJsonMultiPoint(List<Point> points) {

    Assert.notNull(points, "Points must not be null.");
    Assert.isTrue(points.size() >= Constants.TWO, "Minimum of 2 Points required.");

    this.points = new ArrayList<Point>(points);
  }

  /**
   * Creates a new {@link com.digitalbridge.domain.geo.GeoJsonMultiPoint} for the given
   * {@link org.springframework.data.geo.Point}s.
   *
   * @param first must not be {@literal null}.
   * @param second must not be {@literal null}.
   * @param others must not be {@literal null}.
   */
  public GeoJsonMultiPoint(Point first, Point second, Point... others) {

    Assert.notNull(first, "First point must not be null!");
    Assert.notNull(second, "Second point must not be null!");
    Assert.notNull(others, "Additional points must not be null!");

    this.points = new ArrayList<Point>();
    this.points.add(first);
    this.points.add(second);
    this.points.addAll(Arrays.asList(others));
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
  public List<Point> getCoordinates() {
    return Collections.unmodifiableList(this.points);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return ObjectUtils.nullSafeHashCode(this.points);
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

    if (!(obj instanceof GeoJsonMultiPoint)) {
      return false;
    }

    return ObjectUtils.nullSafeEquals(this.points, ((GeoJsonMultiPoint) obj).points);
  }
}
