package com.ls.mobile.geotool.db.data.model;

import android.graphics.Bitmap;

// IMPROVEMENT: we can do a REFACTOR when this improvement could be useful
public class Observation {
    private int mId;
    private String code;
    private String date;
    private double g1;
    private double g2;
    private double g3;
    private double latitude;
    private double longitude;
    private String status;
    private int oneWayValue; // 0 IDA - 1 RETORNO
    private int userId;
    private int lineId;
    private Bitmap g1Photo;
    private Bitmap g2Photo;
    private Bitmap g3Photo;
    private double height;
    private double reducedG;
    private double reading;
    private double offset;
    private int pointId;

    // CALCULATED VALUES ON CssGravityLine.class
    private double delta;
    private double residual;

    public double getDelta() {
        return delta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    public double getResidual() {
        return residual;
    }

    public void setResidual(double residual) {
        this.residual = residual;
    }

    public Observation() {
    }

    public Bitmap getG1Photo() {
        return g1Photo;
    }

    public void setG1Photo(Bitmap g1Photo) {
        this.g1Photo = g1Photo;
    }

    public Bitmap getG2Photo() {
        return g2Photo;
    }

    public void setG2Photo(Bitmap g2Photo) {
        this.g2Photo = g2Photo;
    }

    public Bitmap getG3Photo() {
        return g3Photo;
    }

    public void setG3Photo(Bitmap g3Photo) {
        this.g3Photo = g3Photo;
    }

    public int getmId() {
        return mId;
    }

    public void setmId(int mId) {
        this.mId = mId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getG1() {
        return g1;
    }

    public void setG1(double g1) {
        this.g1 = g1;
    }

    public double getG2() {
        return g2;
    }

    public void setG2(double g2) {
        this.g2 = g2;
    }

    public double getG3() {
        return g3;
    }

    public void setG3(double g3) {
        this.g3 = g3;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getOneWayValue() {
        return oneWayValue;
    }

    public void setOneWayValue(int oneWayValue) {
        this.oneWayValue = oneWayValue;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getLineId() {
        return lineId;
    }

    public void setLineId(int lineId) {
        this.lineId = lineId;
    }


    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }


    public double getReducedG() {
        return reducedG;
    }

    public void setReducedG(double reducedG) {
        this.reducedG = reducedG;
    }


    public double getReading() {
        return reading;
    }

    public void setReading(double reading) {
        this.reading = reading;
    }


    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }


    public int getPointId() {
        return pointId;
    }

    public void setPointId(int pointId) {
        this.pointId = pointId;
    }


}