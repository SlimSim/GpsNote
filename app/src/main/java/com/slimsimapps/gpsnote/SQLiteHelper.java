package com.slimsimapps.gpsnote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.slimsimapps.gpsnote.database.NoteModel;
import com.slimsimapps.gpsnote.database.LastPositionModel;

import java.util.ArrayList;

/**
 * 2016-07-03. Created by Simon
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG = "SQLiteHelper";

    private SQLiteDatabase database;
    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "SQLiteDatabase.db";
    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static final String TABLE_NAME = "GPS_NOTE";
    public static final String COLUMN_ID = "_ID";
    public static final String COLUMN_NOTE = "NOTE";
    public static final String COLUMN_LONGITUDE = "LONGITUDE";
    public static final String COLUMN_LATITUDE = "LATITUDE";

    public static final String LAST_POSS = "LAST_POSSITION";
    public static final String SAVED_TIME = "SAVED_TIME";
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NOTE + " VARCHAR, " + COLUMN_LONGITUDE + " DOUBLE, " + COLUMN_LATITUDE + " DOUBLE);");
        createLastPossTable( db );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion == 2)
            createLastPossTable(db);

    }

    private void createLastPossTable( SQLiteDatabase db ) {
        db.execSQL("create table " + LAST_POSS + " ( " + SAVED_TIME + " LONG, " + COLUMN_LONGITUDE + " DOUBLE, " + COLUMN_LATITUDE + " DOUBLE);");

        LastPositionModel lpm = new LastPositionModel();
        lpm.setLong( 0 );
        lpm.setLat( 0 );
        lpm.setSavedTimeavedTime( System.currentTimeMillis() );

        ContentValues contentValues = new ContentValues();
        contentValues.put(SAVED_TIME, lpm.getSavedTime());
        contentValues.put(COLUMN_LATITUDE, lpm.getLatitude());
        contentValues.put(COLUMN_LONGITUDE, lpm.getLongitude());

        db.insert(LAST_POSS, null, contentValues);
    }

    /**
     * generaly 2 different methods for interacting with the DB,
     * Method 1 is probably the SQL-injection-safe method
     * Method 2 allows for litle more controle :)
     * @param contact the model to be inserted :)
     */

    public void insertRecord(NoteModel contact) { //method 1
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NOTE, contact.getNote());
        contentValues.put(COLUMN_LATITUDE, contact.getLatitude());
        contentValues.put(COLUMN_LONGITUDE, contact.getLongitude());
        database.insert(TABLE_NAME, null, contentValues);
        database.close();
    }
    /*
    public void insertRecordAlternate(NoteModel contact) { //method 2
        database = this.getReadableDatabase();
        database.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_LONGITUDE + "," + COLUMN_LATITUDE + ") VALUES('" + contact.getFirstName() + "','" + contact.getLastName() + "')");
        database.close();
    }
    */

    public void updateRecord(NoteModel contact) { // method 1
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID, contact.getID());
        contentValues.put(COLUMN_NOTE, contact.getNote());
        contentValues.put(COLUMN_LATITUDE, contact.getLatitude());
        contentValues.put(COLUMN_LONGITUDE, contact.getLongitude());
        database.update(TABLE_NAME, contentValues, COLUMN_ID + " = ?", new String[]{""+contact.getID()});
        database.close();
    }
    public void updateLastPoss(LastPositionModel lastPositionModel) { // method 1
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SAVED_TIME, lastPositionModel.getSavedTime());
        contentValues.put(COLUMN_LATITUDE, lastPositionModel.getLatitude());
        contentValues.put(COLUMN_LONGITUDE, lastPositionModel.getLongitude());
        database.update(LAST_POSS, contentValues, null, null);
        database.close();
    }
    /*
    public void updateRecordAlternate(NoteModel contact) { // method 2
        database = this.getReadableDatabase();
        database.execSQL("update " + TABLE_NAME + " set " + COLUMN_LONGITUDE + " = '" + contact.getFirstName() + "', " + COLUMN_LATITUDE + " = '" + contact.getLastName() + "' where " + COLUMN_ID + " = '" + contact.getID() + "'");
        database.close();
    }
    */

    public void deleteRecord(NoteModel contact) { // method 1
        database = this.getReadableDatabase();
//        database.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{ contact.getID()});
        database.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{""+contact.getID()});
        database.close();
    }
    /*
    public void deleteRecordAlternate(NoteModel contact) { // method 2
        database = this.getReadableDatabase();
        database.execSQL("delete from " + TABLE_NAME + " where " + COLUMN_ID + " = '" + contact.getID() + "'");
        database.close();
    }
    */



    public ArrayList<NoteModel> getAllRecords() { // method 1
        database = this.getReadableDatabase();
        Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, null);

        ArrayList<NoteModel> contacts = new ArrayList<>();
        NoteModel noteModel;
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                noteModel = new NoteModel();
                noteModel.setID(cursor.getInt(cursor.getColumnIndex( COLUMN_ID )));
                noteModel.setNote(cursor.getString(cursor.getColumnIndex( COLUMN_NOTE )));
                noteModel.setLong(cursor.getDouble(cursor.getColumnIndex( COLUMN_LONGITUDE )));
                noteModel.setLat(cursor.getDouble(cursor.getColumnIndex( COLUMN_LATITUDE )));


                contacts.add(noteModel);
            }
        }
        cursor.close();
        database.close();

        return contacts;
    }

    public LastPositionModel getLastPoss() { // method 1
        database = this.getReadableDatabase();
        Cursor cursor = database.query(LAST_POSS, null, null, null, null, null, null);

        LastPositionModel lastPositionModel = new LastPositionModel();;
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                lastPositionModel.setSavedTimeavedTime(cursor.getInt(cursor.getColumnIndex( SAVED_TIME )));
                lastPositionModel.setLong(cursor.getDouble(cursor.getColumnIndex( COLUMN_LONGITUDE )));
                lastPositionModel.setLat(cursor.getDouble(cursor.getColumnIndex( COLUMN_LATITUDE )));
            }
        }
        cursor.close();
        database.close();

        return lastPositionModel;
    }
    /*
    public ArrayList<NoteModel> getAllRecordsAlternate() { //method 2
        database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        ArrayList<NoteModel> contacts = new ArrayList<NoteModel>();
        NoteModel contactModel;
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();

                contactModel = new NoteModel();
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
