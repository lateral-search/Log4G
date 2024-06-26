package com.ls.mobile.geotool.gps;
/**
 * This util is used to calculate various points on a sphere from GPS
 * coordinates
 * Represents a point on the surface of a sphere. (The Earth is
 * almost spherical.).To create an instance, call one of the static
 * methods fromDegrees() or fromRadians().
 *
 * This code was originally published at:
 * http://JanMatuschek.de/LatitudeLongitudeBoundingCoordinates#Java
 *
 * @author Jan Philip Matuschek
 *
 * NOTE: never used in log4G but probably could give ideas.
 *
 * @deprecated
 */

public class GeoLocationCalculator {

        private double radLat; // latitude in radians
        private double radLon; // longitude in radians
        private double degLat; // latitude in degrees
        private double degLon; // longitude in degrees

        private static final double MIN_LAT = Math.toRadians(-90d);  // -PI/2
        private static final double MAX_LAT = Math.toRadians(90d);   // PI/2
        private static final double MIN_LON = Math.toRadians(-180d); // -PI
        private static final double MAX_LON = Math.toRadians(180d);  // PI

        private GeoLocationCalculator() {

        }

        /**
         * @param latitude  the latitude, in degrees.
         * @param longitude the longitude, in degrees.
         */
        public static GeoLocationCalculator fromDegrees(double latitude, double longitude) {
            GeoLocationCalculator result = new GeoLocationCalculator();
            result.radLat = Math.toRadians(latitude);
            result.radLon = Math.toRadians(longitude);
            result.degLat = latitude;
            result.degLon = longitude;
            result.checkBounds();
            return result;
        }

        /**
         * @param latitude  the latitude, in radians.
         * @param longitude the longitude, in radians.
         */
        public static GeoLocationCalculator fromRadians(double latitude, double longitude) {
            GeoLocationCalculator result = new GeoLocationCalculator();
            result.radLat = latitude;
            result.radLon = longitude;
            result.degLat = Math.toDegrees(latitude);
            result.degLon = Math.toDegrees(longitude);
            result.checkBounds();
            return result;
        }

        private void checkBounds() {
            if (radLat < MIN_LAT || radLat > MAX_LAT ||
                    radLon < MIN_LON || radLon > MAX_LON) {
                throw new IllegalArgumentException();
            }
        }

        /**
         * @return the latitude, in degrees.
         */
        public double getLatitudeInDegrees() {
            return degLat;
        }

        /**
         * @return the longitude, in degrees.
         */
        public double getLongitudeInDegrees() {
            return degLon;
        }

        /**
         * @return the latitude, in radians.
         */
        public double getLatitudeInRadians() {
            return radLat;
        }

        /**
         * @return the longitude, in radians.
         */
        public double getLongitudeInRadians() {
            return radLon;
        }

        @Override
        public String toString() {
            return "(" + degLat + "\u00B0, " + degLon + "\u00B0) = (" +
                    radLat + " rad, " + radLon + " rad)";
        }

        /**
         * Computes the great circle distance between this GeoLocation instance and
         * the location argument.
         *
         * @param radius the radius of the sphere, e.g. the average radius for a
         *               spherical approximation of the figure of the Earth is
         *               approximately 6371.01 kilometers.
         *
         * @return the distance, measured in the same unit as the radius argument.
         */
        public double distanceTo(GeoLocationCalculator location, double radius) {
            return Math.acos(Math.sin(radLat) * Math.sin(location.radLat) +
                    Math.cos(radLat) * Math.cos(location.radLat) *
                            Math.cos(radLon - location.radLon)) * radius;
        }

        /**
         * Computes the bounding coordinates of all points on the surface of a
         * sphere that have a great circle distance to the point represented by this
         * GeoLocation instance that is less or equal to the distance argument.
         *
         *
         * @param distance the distance from the point represented by this
         *                 GeoLocation instance. Must me measured in the same
         *                 unit as the radius argument.
         *
         * @param radius   the radius of the sphere, e.g. the average radius for a
         *                 spherical approximation of the figure of the Earth is
         *                 approximately 6371.01 kilometers.
         *
         * @return an array of two GeoLocation objects such that:
         *
         * The latitude of any point within the specified distance is
         * greater or equal to the latitude of the first array element and
         * smaller or equal to the latitude of the second array element.
         *
         * If the longitude of the first array element is smaller or
         * equal to the longitude of the second element, then the longitude
         * of any point within the specified distance is greater or equal to
         * the longitude of the first array element and smaller or equal to
         * the longitude of the second array element.
         *
         * If the longitude of the first array element is greater than
         * the longitude of the second element (this is the case if the
         * 180th meridian is within the distance), then the longitude of any
         * point within the specified distance is greater or equal to the
         * longitude of the first array element OR smaller
         * or equal to the longitude of the second array element.
         *
         */
        public GeoLocationCalculator[] boundingCoordinates(double distance, double radius) {

            if (radius < 0d || distance < 0d) {
                throw new IllegalArgumentException();
            }

            // angular distance in radians on a great circle
            double radDist = distance / radius;

            double minLat = radLat - radDist;
            double maxLat = radLat + radDist;

            double minLon, maxLon;
            if (minLat > MIN_LAT && maxLat < MAX_LAT) {
                double deltaLon = Math.asin(Math.sin(radDist) /
                        Math.cos(radLat));
                minLon = radLon - deltaLon;
                if (minLon < MIN_LON) {
                    minLon += 2d * Math.PI;
                }
                maxLon = radLon + deltaLon;
                if (maxLon > MAX_LON) {
                    maxLon -= 2d * Math.PI;
                }
            } else {
                // a pole is within the distance
                minLat = Math.max(minLat, MIN_LAT);
                maxLat = Math.min(maxLat, MAX_LAT);
                minLon = MIN_LON;
                maxLon = MAX_LON;
            }

            return new GeoLocationCalculator[]{
                    fromRadians(minLat, minLon),
                    fromRadians(maxLat, maxLon)
            };
        }

    }