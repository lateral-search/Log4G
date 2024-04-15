package com.ls.mobile.geotool.db.data.model;

public class Calibration {
    private int mId;
    private int calibrationValueIndex;
    private double calibrationValue;
    private int gravimeterId;

    public Calibration(){}

    public int getmId() {
        return mId;
    }

    public void setmId(int mId) {
        this.mId = mId;
    }

    public int getCalibrationValueIndex() {
        return calibrationValueIndex;
    }

    public void setCalibrationValueIndex(int calibrationValueIndex) {
        this.calibrationValueIndex = calibrationValueIndex;
    }

    public double getCalibrationValue() {
        return calibrationValue;
    }

    public void setCalibrationValue(double calibrationValue) {
        this.calibrationValue = calibrationValue;
    }

    public int getGravimeterId() {
        return gravimeterId;
    }

    public void setGravimeterId(int gravimeterId) {
        this.gravimeterId = gravimeterId;
    }



}
