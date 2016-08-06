package com.slimsimapps.gpsnote.database;

/**
 * 2016-07-03, Created by Simon
 * The model for a note, compleate with longitude and latitude
 */
public class LastPositionModel {

    private long savedTime;

    private double longitude, latitude;

    public long getSavedTime() {
        return savedTime;
    }

    public void setSavedTimeavedTime(long savedTime) {
        this.savedTime = savedTime;
    }

    public void setLong(double longitude) {
        this.longitude = longitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLat(double latitude) {
        this.latitude = latitude;
    }

    public double  getLatitude() {
        return this.latitude;
    }

}