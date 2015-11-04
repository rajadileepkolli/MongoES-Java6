package com.digitalbridge.domain.geo;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.geo.Point;

/**
 * <p>
 * GeoJsonPoint class.
 * </p>
 *
 * @author rajakolli
 * @version 1 : 0
 */
public class GeoJsonPoint extends Point implements GeoJson<List<Double>> {

  private static final long serialVersionUID = -8026303425147474002L;

  private static final String TYPE = "Point";

  /**
   * Creates {@link com.digitalbridge.domain.geo.demo.mongoes.model.GeoJsonPoint} for given coordinates.
   *
   * @param x a double.
   * @param y a double.
   */
  public GeoJsonPoint(double x, double y) {
    super(x, y);
  }

  /**
   * <p>
   * Constructor for GeoJsonPoint.
   * </p>
   */
  public GeoJsonPoint() {
    super(0, 0);
  }

  /**
   * Creates {@link com.digitalbridge.domain.geo.demo.mongoes.model.GeoJsonPoint} for given
   * {@link org.springframework.data.geo.Point}.
   *
   * @param point must not be {@literal null}.
   */
  public GeoJsonPoint(Point point) {
    super(point);
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
  public List<Double> getCoordinates() {
    return Arrays.asList(Double.valueOf(getX()), Double.valueOf(getY()));
  }

}
