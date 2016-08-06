package com.slimsimapps.gpsnote.database;

/**
 * 2016-07-03, Created by Simon
 * The model for a note, compleate with longitude and latitude
 */
public class NoteModel {

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