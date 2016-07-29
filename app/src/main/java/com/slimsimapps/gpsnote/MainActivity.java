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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;


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

        readRecord();

//        longitudeValue = (TextView) findViewById(R.id.getLocationLongitudeResult); // my
//        latitudeValue = (TextView) findViewById(R.id.getLocationLatitudeResult); // my

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
        Log.v(TAG, "onSaveGpsNote ->");

        currentNote = ((EditText) findViewById(R.id.textAddNewRecord)).getText().toString();


        EditText editText = ((EditText) findViewById(R.id.textAddNewRecord));
        editText.setVisibility(View.GONE);
        findViewById(R.id.btnSaveNewRecord).setVisibility(View.GONE);
        findViewById(R.id.btnWriteNewRecord).setVisibility(View.VISIBLE);
        findViewById(R.id.btnReadRecord).setVisibility(View.VISIBLE);

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
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        //        criteria.setPowerRequirement(Criteria.POWER_LOW);

        //locationManager.requestSingleUpdate();
        String provider = locationManager.getBestProvider(criteria, false);

        locationManager.requestSingleUpdate(provider, locationListener, Looper.myLooper());

//        Location currentLocation = locationManager.getLastKnownLocation( provider );
    }

    public void getLocationAndSaveNote() {
        Log.v(TAG, "getLocationAndSaveNote ->");
        if(!checkLocation())
            return;
        ((LinearLayout) findViewById(R.id.parentLayout)).addView( getNoteView(currentNote, 0), 0 );

        getLocation( locationListenerToSaveNote );
        Log.v(TAG, "getLocationAndSaveNote <-");
    }

    private View getNoteView(String text, double distance) {
        View child = getLayoutInflater().inflate(R.layout.note, null);
        ((TextView) child.findViewById(R.id.noteNote)).setText( text );
        String strDistance;
//        if( distance > 1000000 )
//            strDistance = Math.round(distance/1000000) + "Mm";
        if( distance > 1000 )
            strDistance = Math.round(distance/1000) + "km";
        else
            strDistance = Math.round(distance) + "m";

        ((TextView) child.findViewById(R.id.noteDistance)).setText( strDistance );
        return child;
    }

    // /*
    private final LocationListener locationListenerToSaveNote = new LocationListener() {

        public void onLocationChanged(Location location) {
            Log.v(TAG, "onLocationCanged -> location = " + location);

            final double longitudeBest = location.getLongitude();
            final double latitudeBest = location.getLatitude();

            Log.v(TAG, "Longitude = " + longitudeBest);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.v(TAG, "run ->");
//                    setCorr(longitudeBest, latitudeBest);

                    ContactModel gpsNote = new ContactModel();

                    gpsNote.setNote(currentNote);
                    gpsNote.setLong(longitudeBest);
                    gpsNote.setLat(latitudeBest);


                    Log.v(TAG, "-> sQLiteHelper.insertRecord");
                    sQLiteHelper.insertRecord( gpsNote );
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
            Log.v(TAG, "onLocationCanged -> location = " + location);

            final Coordinate currentPoss = new Coordinate(
                    location.getLongitude(), location.getLatitude());
//            final double longitudeBest = location.getLongitude();
//            final double latitudeBest = location.getLatitude();

            final ArrayList<ContactModel> arcm = sQLiteHelper.getAllRecords();

            Log.v(TAG, "arcm = " + arcm.toString());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.v(TAG, "run ->");
//                    setCorr(longitudeBest, latitudeBest);

                    ArrayList<Double> aDist = new ArrayList<>();
                    ArrayList<String> aText = new ArrayList<>();

                    Log.v(TAG, "arcm.size() = " + arcm.size());
                    for(int i=0; i<arcm.size(); i++) {

                        Coordinate c1 = new Coordinate(
                                arcm.get(i).getLongitude(), arcm.get(i).getLatitude());

                        double dist = currentPoss.dist(c1); // distance in meter

                        String text = arcm.get(i).getNote();
                        boolean added = false;

                        for(int j=0; j<aDist.size(); j++) {
                            if( dist < aDist.get(j) ) {
                                aDist.add(j, dist);
                                aText.add(j, text);
                                added = true;
                                break;
                            }
                        }
                        if( !added ) {
                            aDist.add(dist);
                            aText.add(text);
                        }
                    }
                    ((LinearLayout) findViewById(R.id.parentLayout)).removeAllViewsInLayout();
                    for(int i=0; i<aText.size(); i++) {
//                        TextView textView = new TextView( m_context ); //this should be "this", men då får man spara det annanstans ifrån :)
//                        textView.setText( aText.get(i) );
                        View note = getNoteView( aText.get(i), aDist.get(i));
                        ((LinearLayout) findViewById(R.id.parentLayout)).addView( note );
                    }
                    if( aText.size() == 0 ) {
                        findViewById(R.id.tvNoRecordsFound).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.tvNoRecordsFound).setVisibility(View.GONE);
                    }
                    findViewById(R.id.tvLoadingNotes).setVisibility(View.GONE);


                    Toast.makeText( m_context, "Read all notes", Toast.LENGTH_LONG).show();

                    Log.v(TAG, "locationListenerToCalcDistance / onLocationChanged, updatet list");
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

/*
    private void setCorr(double longitude, double latitude) {
//        longitudeValue.setText(longitude + "");
//        latitudeValue.setText(latitude + "");
        Toast.makeText(MainActivity.this, "Best Provider update", Toast.LENGTH_SHORT).show();

    }
*/

    public void onReadRecord(View view) {

        Log.v(TAG, "onReadRecord ->");
        readRecord();
        Log.v(TAG, "onReadRecord <-");
    }

    private void readRecord() {

        m_context = this;

        getLocation( locationListenerToCalcDistance );

    }

    public void onWriteGpsNote(View view) {
        EditText editText = ((EditText) findViewById(R.id.textAddNewRecord));
        editText.setVisibility(View.VISIBLE);
        findViewById(R.id.btnSaveNewRecord).setVisibility(View.VISIBLE);
        findViewById(R.id.btnWriteNewRecord).setVisibility(View.GONE);
        findViewById(R.id.btnReadRecord).setVisibility(View.GONE);

        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

    }

    public void onClickNote(View view) {
        Log.v(TAG, "onClickNote ->");
        Log.v(TAG, "view " + ((TextView) view.findViewById(R.id.noteNote)).getText());

    }

// */
}


/*
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
*/