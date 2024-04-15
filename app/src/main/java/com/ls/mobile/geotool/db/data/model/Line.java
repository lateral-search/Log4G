package com.ls.mobile.geotool.db.data.model;

public class Line {
    private int mId;
    private String name;
    private String date;
    private String status;
    private double driftRate;
    private int gravimeterId;
    private int calibrationId;
    private int userId;

    public double getDriftRate() {
        return driftRate;
    }

    public void setDriftRate(double driftRate) {
        this.driftRate = driftRate;
    }

    public Line(){}

    public int getmId() {
        return mId;
    }

    public void setmId(int mId) {
        this.mId = mId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getCalibrationId() {
        return calibrationId;
    }

    public void setCalibrationId(int calibrationId) {
        this.calibrationId = calibrationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getGravimeterId() {
        return gravimeterId;
    }

    public void setGravimeterId(int gravimeterId) {
        this.gravimeterId = gravimeterId;
    }
}
