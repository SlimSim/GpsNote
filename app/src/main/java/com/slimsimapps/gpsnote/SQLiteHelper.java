package com.slimsimapps.gpsnote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by simon on 2016-07-03.
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG = "SQLiteHelper";

    private SQLiteDatabase database;
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SQLiteDatabase.db";
    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static final String TABLE_NAME = "GPS_NOTE";
    public static final String COLUMN_ID = "_ID";
    public static final String COLUMN_NOTE = "NOTE";
    public static final String COLUMN_LONGITUDE = "LONGITUDE";
    public static final String COLUMN_LATITUDE = "LATITUDE";
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NOTE + " VARCHAR, " + COLUMN_LONGITUDE + " DOUBLE, " + COLUMN_LATITUDE + " DOUBLE);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * generaly 2 different methods for interacting with the DB,
     * Method 1 is probably the SQL-injection-safe method
     * Method 2 allows for litle more controle :)
     * @param contact
     */

    public void insertRecord(ContactModel contact) { //method 1
        Log.v(TAG, "insertRecord ->");
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NOTE, contact.getNote());
        contentValues.put(COLUMN_LATITUDE, contact.getLatitude());
        contentValues.put(COLUMN_LONGITUDE, contact.getLongitude());
        database.insert(TABLE_NAME, null, contentValues);
        database.close();
        Log.v(TAG, "insertRecord <-");
    }
    /*
    public void insertRecordAlternate(ContactModel contact) { //method 2
        database = this.getReadableDatabase();
        database.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_LONGITUDE + "," + COLUMN_LATITUDE + ") VALUES('" + contact.getFirstName() + "','" + contact.getLastName() + "')");
        database.close();
    }
    */

    public void updateRecord(ContactModel contact) { // method 1
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID, contact.getID());
        contentValues.put(COLUMN_NOTE, contact.getNote());
        contentValues.put(COLUMN_LATITUDE, contact.getLatitude());
        contentValues.put(COLUMN_LONGITUDE, contact.getLongitude());
        database.update(TABLE_NAME, contentValues, COLUMN_ID + " = ?", new String[]{""+contact.getID()});
        database.close();
    }
    /*
    public void updateRecordAlternate(ContactModel contact) { // method 2
        database = this.getReadableDatabase();
        database.execSQL("update " + TABLE_NAME + " set " + COLUMN_LONGITUDE + " = '" + contact.getFirstName() + "', " + COLUMN_LATITUDE + " = '" + contact.getLastName() + "' where " + COLUMN_ID + " = '" + contact.getID() + "'");
        database.close();
    }
    */

    public void deleteRecord(ContactModel contact) { // method 1
        database = this.getReadableDatabase();
//        database.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{ contact.getID()});
        database.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{""+contact.getID()});
        database.close();
    }
    /*
    public void deleteRecordAlternate(ContactModel contact) { // method 2
        database = this.getReadableDatabase();
        database.execSQL("delete from " + TABLE_NAME + " where " + COLUMN_ID + " = '" + contact.getID() + "'");
        database.close();
    }
    */

    public ArrayList<ContactModel> getAllRecords() { // method 1
        database = this.getReadableDatabase();
        Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, null);

        ArrayList<ContactModel> contacts = new ArrayList<ContactModel>();
        ContactModel contactModel;
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                contactModel = new ContactModel();
                contactModel.setID(cursor.getInt(cursor.getColumnIndex( COLUMN_ID )));
                contactModel.setNote(cursor.getString(cursor.getColumnIndex( COLUMN_NOTE )));
                contactModel.setLong(cursor.getDouble(cursor.getColumnIndex( COLUMN_LONGITUDE )));
                contactModel.setLat(cursor.getDouble(cursor.getColumnIndex( COLUMN_LATITUDE )));


                contacts.add(contactModel);
            }
        }
        cursor.close();
        database.close();

        return contacts;
    }
    /*
    public ArrayList<ContactModel> getAllRecordsAlternate() { //method 2
        database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        ArrayList<ContactModel> contacts = new ArrayList<ContactModel>();
        ContactModel contactModel;
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();

                contactModel = new ContactModel();
                contactModel.setID(cursor.getString(0));
                contactModel.setFirstName(cursor.getString(1));
                contactModel.setLastName(cursor.getString(2));

                contacts.add(contactModel);
            }
        }
        cursor.close();
        database.close();

        return contacts;
    }
    */

}
