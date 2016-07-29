package com.slimsimapps.gpsnote;

/**
 * Created by simon on 2016-07-03.
 */
public class ContactModel {

    private int ID;

    private String note;

    private double longitude, latitude;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
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

    public void setNote(String note) {
        this.note = note;
    }

    public String getNote() {
        return this.note;
    }

}