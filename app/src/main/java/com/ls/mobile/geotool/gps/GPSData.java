package com.ls.mobile.geotool.gps;

/**
 * Bean actually not in use.
 *
 * @author lateralsearch
 *
 * @deprecated
 */
public class GPSData {
    private double latitude;
    private double longitude;
    private double velocity;
    private double h;
    private double gravity;

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getH() {
        return h;
    }

    public void setH(double h) {
        this.h = h;
    }

    public double getGravity() {
        return gravity;
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }
}
