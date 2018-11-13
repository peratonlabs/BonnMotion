/*******************************************************************************
 ** BonnMotion - a mobility scenario generation and analysis tool             **
 ** Copyright (C) 2002-2012 University of Bonn                                **
 ** Copyright (C) 2012-2016 University of Osnabrueck                          **
 **                                                                           **
 ** This program is free software; you can redistribute it and/or modify      **
 ** it under the terms of the GNU General Public License as published by      **
 ** the Free Software Foundation; either version 2 of the License, or         **
 ** (at your option) any later version.                                       **
 **                                                                           **
 ** This program is distributed in the hope that it will be useful,           **
 ** but WITHOUT ANY WARRANTY; without even the implied warranty of            **
 ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             **
 ** GNU General Public License for more details.                              **
 **                                                                           **
 ** You should have received a copy of the GNU General Public License         **
 ** along with this program; if not, write to the Free Software               **
 ** Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA **
 *******************************************************************************/

package edu.bonn.cs.iv.util.maps;

import java.awt.geom.Point2D;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.bonn.cs.iv.bonnmotion.Position;
import edu.bonn.cs.iv.bonnmotion.models.slaw.PositionInterface;

import net.sf.geographiclib.*;

/**
 * Class for geographic positions (longitude, latitude)
 *
 * @author schwamborn
 *
 */
public class PositionGeo implements PositionInterface {
    private final double lon;
    private final double lat;

    public PositionGeo(double lon, double lat) {
        assert (lon >= -180.0 && lon <= 180.0);
        assert (lat >= -90.0 && lat <= 90.0);

        this.lon = lon;
        this.lat = lat;
    }

    public PositionGeo(PositionGeo other) {
        this.lon = other.lon();
        this.lat = other.lat();
    }

    public double lon() {
        return lon;
    }

    public double lat() {
        return lat;
    }

    public double x() {
        return lon;
    }

    public double y() {
        return lat;
    }

    /**
     * geodesic longitudinal distance
     */
    public double distanceX(double lon2) {
        PositionGeo p2 = new PositionGeo(lon2, this.lat);
        return distance(p2);
    }

    /**
     * geodesic latitudinal distance
     */
    public double distanceY(double lat2) {
        PositionGeo p2 = new PositionGeo(this.lon, lat2);
        return distance(p2);
    }

    /**
     * geodesic (shortest path between two geographic points) distance
     */
    public double distance(PositionGeo p) {
        return distance(this.lat, this.lon, p.lat(), p.lon());
    }

    /**
     * geodesic (shortest path between two geographic points) distance
     */
    public double distance(double lon2, double lat2) {
        return distance(this.lat, this.lon, lat2, lon2);
    }

    /**
     * geodesic shift
     */
    public PositionGeo shift(double angle, double dist) throws Exception {
        GeodesicData gd = Geodesic.WGS84.Direct(this.lat, this.lon, angle, dist,
                GeodesicMask.LATITUDE | GeodesicMask.LONGITUDE);
        return new PositionGeo(gd.lon2, gd.lat2);
    }

    public PositionGeo shiftX(double offset) throws Exception {
        return new PositionGeo(this.lon + offset, this.lat);
    }

    public PositionGeo shiftY(double offset) throws Exception {
        return new PositionGeo(this.lon, this.lat + offset);
    }

    public boolean equals(PositionGeo p) {
        return (p.x() == this.x() && p.y() == this.y());
    }

    @Override
    public String toString() {
        // return lon + " " + lat;
        return "[" + lon + "," + lat + "],";
    }

    public Point2D.Double transform(CoordinateTransformation t) {
        return t.transform(this.lon, this.lat);
    }

    public Position toPosition(BoundingBox bb) {
        return bb.lonLatToScenario(this);
    }

    /**
     * geodesic (shortest path between two geographic points) distance
     */
    public static double distance(double lat1, double lon1, double lat2,
            double lon2) {
        double result = -1.0;

        try {
            GeodesicData gd = Geodesic.WGS84.Inverse(lat1, lon1, lat2, lon2,
                    GeodesicMask.DISTANCE);
            result = gd.s12;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }

        return result;
    }

    @Override
    public double distance(PositionInterface p) {
        assert (p instanceof PositionGeo);
        return distance((PositionGeo) p);
    }

    public PositionGeo getPhantomNode(PositionGeo p, double alpha) {
        assert (alpha >= 0 && alpha <= 1);
        PositionGeo result = null;

        try {
            GeodesicData gd = Geodesic.WGS84.Inverse(this.lat, this.lon,
                    p.lat(), p.lon(),
                    GeodesicMask.AZIMUTH ^ GeodesicMask.DISTANCE);
            gd = Geodesic.WGS84.Direct(this.lat, this.lon, gd.azi1,
                    gd.s12 * alpha,
                    GeodesicMask.LONGITUDE ^ GeodesicMask.LATITUDE);
            result = new PositionGeo(gd.lon2, gd.lat2);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }

        return result;
    }

    private static Pattern ISO6709_H_LATITUDE = Pattern
            .compile("(?<sign>[+-])" + "(?<deg>\\d{2})" + "((?<min>\\d{2})"
                    + "(?<sec>\\d{2})?)?" + "(\\.(?<fraction>\\d+))?");

    private static Pattern ISO6709_H_LONGITUDE = Pattern
            .compile("(?<sign>[+-])" + "(?<deg>\\d{3})" + "((?<min>\\d{2})"
                    + "(?<sec>\\d{2})?)?" + "(\\.(?<fraction>\\d+))?");

    private static Pattern ISO6709_APPENDIX_H = Pattern
            .compile("(?<latitude>[+-]\\d+(\\.\\d+)?)"
                    + "(?<longitude>[+-]\\d+(\\.\\d+)?)");

    /**
     * Return the named group of the matcher parsed as an Integer
     *
     * @param m
     *            the matcher
     * @param groupname
     *            The name of the group to return from the matcher
     * @return the parsed integer, may be null
     */
    private static Integer getPositionPart(Matcher m, String groupname) {
        Integer retval = null;

        String group = m.group(groupname);

        if (group != null) {
            retval = Integer.parseInt(group, 10);
        }

        return retval;
    }

    /**
     * @see #getPositionPart(Matcher, String)
     * @param m
     * @param groupname
     * @param max
     * @return the numeric value of the named group. Double.NaN if the value is
     *         greater than max.
     */
    private static Double getPositionPart(Matcher m, String groupname,
            int max) {
        Double retval;

        Integer intValue = getPositionPart(m, groupname);

        if (intValue == null) {
            retval = null;
        } else if (intValue <= max) {
            retval = intValue.doubleValue();
        } else {
            retval = Double.NaN;
        }

        return retval;
    }

    /**
     * Parse the part (latitude or longitude) of the position
     * 
     * @param pattern
     *            The pattern with which to parse the part
     * @param part
     *            The part to parse
     * @return The value of the part in the interval [-maxdegree, maxdegree].
     *         Return NaN if cannot be parsed correctly
     */
    private static double parsePositionPart(Pattern pattern, String part,
            int maxdegree) {
        double retval = Double.NaN;

        Matcher m = pattern.matcher(part);

        if (m.matches()) {
            double scale = 1.0;
            retval = getPositionPart(m, "deg", maxdegree);

            Double min = getPositionPart(m, "min", 60);
            if (min != null) {
                retval *= 60.0;
                scale *= 60.0;
                retval += min;
            }

            Double sec = getPositionPart(m, "sec", 60);
            if (sec != null) {
                retval *= 60.0;
                scale *= 60.0;
                retval += sec;
            }

            Integer fraction = getPositionPart(m, "fraction");
            if (fraction != null) {
                retval += Double.parseDouble("0." + fraction);
            }

            boolean isnegative = m.group("sign").equals("-");
            retval = retval / scale * (isnegative ? -1.0 : 1.0);
        }

        return retval;
    }

    public static PositionGeo parsePositionGeo(String position) {
        PositionGeo retval = null;
        Matcher m = ISO6709_APPENDIX_H.matcher(position);

        if (!m.matches()) {
            throw new IllegalArgumentException(
                    "Postion '" + position + "' is no parsable as ISO6709");
        } else {
            double latitude = parsePositionPart(ISO6709_H_LATITUDE,
                    m.group("latitude"), 90);

            double longitude = parsePositionPart(ISO6709_H_LONGITUDE,
                    m.group("longitude"), 180);

            if (Double.isNaN(latitude)) {
                throw new IllegalArgumentException(
                        "Postion '" + m.group("latitude")
                                + "' is not a parsable ISO6709 latitude");
            } else if (Double.isNaN(longitude)) {
                throw new IllegalArgumentException(
                        "Postion '" + m.group("longitude")
                                + "' is not a parsable ISO6709 longitude");
            } else {

                retval = new PositionGeo(longitude, latitude);
            }

            return retval;
        }
    }
}
