package com.slimsimapps.gpsnote;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.slimsimapps.gpsnote.database.LastPositionModel;
import com.slimsimapps.gpsnote.database.NoteModel;

import java.util.ArrayList;


/**
 *
 * tabel
 * 1)   location    note1   note2
 * 2)
 *
 *
 */


public class MainActivity extends AppCompatActivity {

    LocationManager locationManager;
    private String currentNote;
    private Context m_context;


    SQLiteHelper sQLiteHelper = new SQLiteHelper(MainActivity.this);

    // private ArrayList<NoteModel> aContactModel;

    /*
        double longitudeBest, latitudeBest;
        double longitudeGPS, latitudeGPS;
        double longitudeNetwork, latitudeNetwork;
        TextView longitudeValueBest, latitudeValueBest;
        TextView longitudeValue, latitudeValue; // my
        TextView longitudeValueGPS, latitudeValueGPS;
        TextView longitudeValueNetwork, latitudeValueNetwork;
    */
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        m_context = this;

        LastPositionModel lpm = sQLiteHelper.getLastPoss();
        Coordinate lastPoss = new Coordinate(lpm.getLongitude(), lpm.getLatitude());
        updateNoteList( lastPoss, false );

        /*
         * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
         * performs a swipe-to-refresh gesture.
         */
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        if(swipeRefreshLayout != null)
            swipeRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            swipeRefreshLayout.setRefreshing(false);
                            readRecord();
                        }
                    }
            );

        readRecord();

    }

    private boolean checkLocation() {
        if(!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    /**********************************************************************************************
     *
     * Methods that I have written!
     *
     **********************************************************************************************/

    public void onSaveGpsNote(View view) {

        currentNote = ((EditText) findViewById(R.id.textAddNewRecord)).getText().toString();


        EditText editText = ((EditText) findViewById(R.id.textAddNewRecord));
        editText.setVisibility(View.GONE);
        findViewById(R.id.btnSaveNewRecord).setVisibility(View.GONE);
        findViewById(R.id.btnWriteNewRecord).setVisibility(View.VISIBLE);

        editText.setText(null);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        findViewById(R.id.tvNoRecordsFound).setVisibility(View.GONE);
        findViewById(R.id.tvLoadingNotes).setVisibility(View.GONE);

        if(currentNote.isEmpty()) return;

        getLocationAndSaveNote();


//        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

    }

    public void getLocation( LocationListener locationListener) {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        //criteria.setPowerRequirement(Criteria.POWER_LOW);

        String provider = locationManager.getBestProvider(criteria, false);

        Location currentLocation = locationManager.getLastKnownLocation( provider );

        if( currentLocation != null ) {
            if (currentLocation.getAccuracy() > 50) {
                locationManager.requestSingleUpdate(provider, locationListener, Looper.myLooper());
            } else {
                locationListener.onLocationChanged(currentLocation);
                locationManager.requestSingleUpdate(provider, locationListenerToCalcDistance, Looper.myLooper());
            }
        } else {
            locationManager.requestSingleUpdate(provider, locationListenerToCalcDistance, Looper.myLooper());
        }
    }

    public void getLocationAndSaveNote() {
        if(!checkLocation())
            return;

        ((TextView) findViewById(R.id.noteLoadingNote)).setText( currentNote );
        findViewById(R.id.noteLoading).setVisibility( View.VISIBLE );

        getLocation( locationListenerToSaveNote );
    }

    private View getNoteView(String text, double distance, int id) {
        View child = getLayoutInflater().inflate(R.layout.note, null);
        ((TextView) child.findViewById(R.id.noteNote)).setText( text );
        ((TextView) child.findViewById(R.id.noteId)).setText( "" + id );
        String strDistance;
        if( distance > 1000 )
            strDistance = Math.round(distance/1000) + "km";
        else
            strDistance = Math.round(distance) + "m";

        ((TextView) child.findViewById(R.id.noteDistance)).setText( strDistance );
        return child;
    }

    /*
    private View getLoadingNoteView(String text) {

        View child = getLayoutInflater().inflate(R.layout.note, null);
    }
    */

    private final LocationListener locationListenerToSaveNote = new LocationListener() {

        public void onLocationChanged(Location location) {

            final double longitudeBest = location.getLongitude();
            final double latitudeBest = location.getLatitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    NoteModel gpsNote = new NoteModel();

                    gpsNote.setNote(currentNote);
                    gpsNote.setLong(longitudeBest);
                    gpsNote.setLat(latitudeBest);

                    sQLiteHelper.insertRecord( gpsNote );
                    updateNoteList(new Coordinate(longitudeBest, latitudeBest));

                    Toast.makeText( m_context, "Note saved", Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
    private final LocationListener locationListenerToCalcDistance = new LocationListener() {

        public void onLocationChanged(Location location) {

            final Coordinate currentPoss = new Coordinate(
                    location.getLongitude(), location.getLatitude());

            updateNoteList( currentPoss );
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private void readRecord() {

        findViewById(R.id.updatePositionSpinner).setVisibility( View.VISIBLE );
        getLocation( locationListenerToCalcDistance );

    }

    public void onWriteGpsNote(View view) {
        EditText editText = ((EditText) findViewById(R.id.textAddNewRecord));
        editText.setVisibility(View.VISIBLE);
        findViewById(R.id.btnSaveNewRecord).setVisibility(View.VISIBLE);
        findViewById(R.id.btnWriteNewRecord).setVisibility(View.GONE);

        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

    }

    public void onClickNote(View view) {
        View v = ((View) view.getParent()).findViewById(R.id.noteButtons);

        if( v.getVisibility() == View.VISIBLE ){
            v.setVisibility( View.GONE );
        } else {
            hideAllNoteButtons();
            v.setVisibility( View.VISIBLE );
        }
    }

    private void updateNoteList(final Coordinate currentPoss ){
        updateNoteList(currentPoss, true);
    }

    private void updateNoteList(final Coordinate currentPoss, boolean bUpdateDbLastPos ){

        if( bUpdateDbLastPos ) {
            LastPositionModel lpm = new LastPositionModel();
            lpm.setLong(currentPoss.getLong());
            lpm.setLat(currentPoss.getLat());
            lpm.setSavedTimeavedTime(System.currentTimeMillis());
            sQLiteHelper.updateLastPoss(lpm);
            findViewById(R.id.updatePositionSpinner).setVisibility( View.GONE );
        }

        final ArrayList<NoteModel> aNoteModel = sQLiteHelper.getAllRecords();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Double> aDist = new ArrayList<>();
                ArrayList<String> aText = new ArrayList<>();
                ArrayList<Integer> aId = new ArrayList<>();

                for(int i = 0; i< aNoteModel.size(); i++) {
                    Coordinate c1 = new Coordinate(
                            aNoteModel.get(i).getLongitude(),
                            aNoteModel.get(i).getLatitude());

                    double dist = currentPoss.dist(c1); // distance in meter
                    int id = aNoteModel.get(i).getID();
                    String text = aNoteModel.get(i).getNote();
                    boolean added = false;

                    for(int j=0; j<aDist.size(); j++) {
                        if( dist < aDist.get(j) ) {
                            aDist.add(j, dist);
                            aText.add(j, text);
                            aId.add(j, id);
                            added = true;
                            break;
                        }
                    }
                    if( !added ) {
                        aDist.add(dist);
                        aText.add(text);
                        aId.add(id);
                    }
                }
                ((LinearLayout) findViewById(R.id.parentLayout)).removeAllViewsInLayout();
                for(int i=0; i<aText.size(); i++) {
                    View note = getNoteView( aText.get(i), aDist.get(i), aId.get(i));
                    ((LinearLayout) findViewById(R.id.parentLayout)).addView( note );
                }

                if( aText.size() == 0 ) {
                    findViewById(R.id.tvNoRecordsFound).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.tvNoRecordsFound).setVisibility(View.GONE);
                }
                findViewById(R.id.tvLoadingNotes).setVisibility(View.GONE);
//                Toast.makeText( m_context, "Read all notes", Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.noteLoading).setVisibility( View.GONE );
    }

    private View getNote(Button view){
        return ((View) view.getParent().getParent());
    }

    private NoteModel getContact(View note){

        int id = Integer.parseInt(""+((TextView) note.findViewById(R.id.noteId)).getText());
        ArrayList<NoteModel> aNoteModel = sQLiteHelper.getAllRecords();

        for(NoteModel cm : aNoteModel){
            if( cm.getID() == id )
                return cm;
        }
        return null;
    }

    public void deleteNote(final View view) {
        new AlertDialog.Builder(m_context)
                .setTitle("Delete note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        View note = getNote((Button) view);
                        NoteModel cm = getContact( note );
                        sQLiteHelper.deleteRecord( cm );

                        ((ViewGroup) note.getParent()).removeView(note);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void editNote(final View view) {
        final View note = getNote( (Button) view );
        final EditText editText = new EditText(m_context);
        editText.setText(((TextView) note.findViewById(R.id.noteNote)).getText());
        new AlertDialog.Builder(m_context)
                .setTitle("Edit note")
                .setView(editText)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String text = "" + editText.getText();
                        NoteModel cm = getContact( note );
                        cm.setNote( text );
                        sQLiteHelper.updateRecord( cm );
                        ((TextView) note.findViewById(R.id.noteNote)).setText( text );
                        hideAllNoteButtons();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    public void updateNotePosition(final View view) {
        new AlertDialog.Builder(m_context)
                .setTitle("Update note position")
                .setMessage("Are you sure you want to update this notes position to current position?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        hideAllNoteButtons();
                        final View note = getNote( (Button) view );
                        note.findViewById(R.id.noteDistance).setVisibility(View.GONE);
                        note.findViewById(R.id.noteDistanceLoading).setVisibility(View.VISIBLE);
                        getLocation(new LocationListener() {
                            public void onLocationChanged(Location location) {

                                final double longitudeBest = location.getLongitude();
                                final double latitudeBest = location.getLatitude();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        NoteModel cm = getContact( note );
                                        cm.setLong( longitudeBest );
                                        cm.setLat( latitudeBest );
                                        sQLiteHelper.updateRecord( cm );

                                        updateNoteList(new Coordinate(longitudeBest, latitudeBest));

                                        ((TextView) note.findViewById(R.id.noteDistance)).setText(
                                                "Updating..."
                                        );

                                        Toast.makeText( m_context, "Note updated", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            @Override
                            public void onStatusChanged(String s, int i, Bundle bundle) {

                            }

                            @Override
                            public void onProviderEnabled(String s) {

                            }

                            @Override
                            public void onProviderDisabled(String s) {

                            }
                        });

                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_map)
                .show();
    }



    public void hideAllNoteButtons(View view) {
        hideAllNoteButtons();
    }

    private void hideAllNoteButtons(){
        ViewGroup vg = (ViewGroup) findViewById(R.id.parentLayout);
        ArrayList<View> views = getViewsByTag( vg, "noteButtons" );

        for(int i=0; i<views.size(); i++){
            views.get(i).setVisibility( View.GONE );
        }
    }

    private static ArrayList<View> getViewsByTag(ViewGroup root, String tag){
        ArrayList<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }
        return views;
    }
}