package com.digitalbridge.mongodb.convert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.geo.Point;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.digitalbridge.domain.geo.GeoJson;
import com.digitalbridge.domain.geo.GeoJsonGeometryCollection;
import com.digitalbridge.domain.geo.GeoJsonPoint;
import com.digitalbridge.domain.geo.GeoJsonPolygon;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Wrapper class to contain useful geo structure converters adhering to the GeoJSON format for the usage with Mongo.
 *
 * @author rajakolli
 * @version 1:0
 */
public class GeoJsonConverters {
  /** Constant <code>COORDINATES="coordinates"</code> */
  public static final String COORDINATES = "coordinates";

  private GeoJsonConverters() {

  }

  /**
   * Returns the geo converters to be registered.
   *
   * @return a {@link java.util.List} object.
   */
  public static List<Converter<?, ?>> getConvertersToRegister() {
    List<Converter<?, ?>> list = new ArrayList<Converter<?, ?>>();
    list.add(DbObjectToGeoJsonPointConverter.INSTANCE);
    list.add(DbObjectToGeoJsonPolygonConverter.INSTANCE);
    list.add(GeoJsonPointToDBObjectConverter.INSTANCE);
    list.add(GeoJsonPolygonToDbObjectConverter.INSTANCE);
    return list;
  }

  /**
   * Converts a {@link org.springframework.data.geo.Point} into a {@link com.mongodb.DBObject} adhering to the GeoJSON
   * format.
   */
  @WritingConverter
  public static enum GeoJsonPointToDBObjectConverter implements Converter<GeoJsonPoint, DBObject> {
    INSTANCE;

    /* (non-Javadoc)
     * @see org.springframework.core.convert.converter.Converter#convert(java.lang.Object)
     */
    @Override
    public DBObject convert(GeoJsonPoint source) {
      return GeoJsonToDbObjectConverter.INSTANCE.convert(source);
    }
  }

  @SuppressWarnings("rawtypes")
  static enum GeoJsonToDbObjectConverter implements Converter<GeoJson, DBObject> {
    INSTANCE;

    /*
     * (non-Javadoc)
     * @see org.springframework.core.convert.converter.Converter#convert(java.lang.Object)
     */
    @Override
    public DBObject convert(GeoJson source) {

      if (source == null) {
        return null;
      }

      DBObject dbo = new BasicDBObject("type", source.getType());

      if (source instanceof GeoJsonGeometryCollection) {

        BasicDBList dbl = new BasicDBList();

        for (GeoJson geometry : ((GeoJsonGeometryCollection) source).getCoordinates()) {
          dbl.add(convert(geometry));
        }

        dbo.put("geometries", dbl);

      } else {
        dbo.put(COORDINATES, convertIfNecessarry(source.getCoordinates()));
      }

      return dbo;
    }

    private Object convertIfNecessarry(Object candidate) {

      if (candidate instanceof GeoJson) {
        return convertIfNecessarry(((GeoJson) candidate).getCoordinates());
      }

      if (candidate instanceof Iterable) {

        BasicDBList dbl = new BasicDBList();

        for (Object element : (Iterable) candidate) {
          dbl.add(convertIfNecessarry(element));
        }

        return dbl;
      }

      if (candidate instanceof Point) {
        return toList((Point) candidate);
      }

      return candidate;
    }

    private List<Double> toList(Point point) {
      return Arrays.asList(point.getX(), point.getY());
    }
  }

  /**
   * Converts a {@link com.mongodb.DBObject} adhering to the GeoJSON format into a
   * {@link org.springframework.data.geo.Point}.
   */
  @ReadingConverter
  public static enum DbObjectToGeoJsonPointConverter implements Converter<DBObject, GeoJsonPoint> {
    INSTANCE;

    @SuppressWarnings("unchecked")
    @Override
    public GeoJsonPoint convert(DBObject source) {

      if (source == null) {
        return null;
      }

      Assert.isTrue(ObjectUtils.nullSafeEquals(source.get("type"), "Point"),
          String.format("Cannot convert type '%s' to Point.", source.get("type")));

      List<Double> dbl = (List<Double>) source.get(COORDINATES);
      return new GeoJsonPoint(dbl.get(0).doubleValue(), dbl.get(1).doubleValue());
    }
  }

  /**
   * Converts a {@link org.springframework.data.geo.Polygon} into a {@link com.mongodb.DBObject} adhering to the GeoJSON
   * format.
   */
  @WritingConverter
  public static enum GeoJsonPolygonToDbObjectConverter implements Converter<GeoJsonPolygon, DBObject> {

    INSTANCE;

    /*
     * (non-Javadoc)
     * @see org.springframework.core.convert.converter.Converter#convert(java.lang.Object)
     */
    @Override
    public DBObject convert(GeoJsonPolygon source) {
      return GeoJsonToDbObjectConverter.INSTANCE.convert(source);
    }
  }

  /**
   * Converts a {@link com.mongodb.DBObject} adhering to the GeoJSON format into a
   * {@link org.springframework.data.geo.Polygon}.
   */
  @ReadingConverter
  public static enum DbObjectToGeoJsonPolygonConverter implements Converter<DBObject, GeoJsonPolygon> {

    INSTANCE;

    /*
     * (non-Javadoc)
     * @see org.springframework.core.convert.converter.Converter#convert(java.lang.Object)
     */
    @Override
    public GeoJsonPolygon convert(DBObject source) {

      if (source == null) {
        return null;
      }

      Assert.isTrue(ObjectUtils.nullSafeEquals(source.get("type"), "Polygon"),
          String.format("Cannot convert type '%s' to Polygon.", source.get("type")));

      return toGeoJsonPolygon((BasicDBList) source.get("coordinates"));
    }
  }

  /**
   * Converts a coordinate pairs nested in in {@link BasicDBList} into {@link GeoJsonPoint}s.
   * 
   * @param listOfCoordinatePairs
   * @return
   * @since 1.7
   */
  @SuppressWarnings("unchecked")
  static List<Point> toListOfPoint(BasicDBList listOfCoordinatePairs) {

    List<Point> points = new ArrayList<Point>();

    for (Object point : listOfCoordinatePairs) {

      Assert.isInstanceOf(List.class, point);

      List<Double> coordinatesList = (List<Double>) point;

      points.add(new GeoJsonPoint(coordinatesList.get(0).doubleValue(), coordinatesList.get(1).doubleValue()));
    }
    return points;
  }

  /**
   * Converts a coordinate pairs nested in in {@link BasicDBList} into {@link GeoJsonPolygon}.
   * 
   * @param dbList
   * @return
   * @since 1.7
   */
  static GeoJsonPolygon toGeoJsonPolygon(BasicDBList dbList) {
    return new GeoJsonPolygon(toListOfPoint((BasicDBList) dbList.get(0)));
  }
}
