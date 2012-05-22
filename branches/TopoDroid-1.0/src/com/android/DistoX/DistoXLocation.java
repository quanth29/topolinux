/* @file DistoXLocation.java
 *
 * @author marco corvi
 * @date dec 2011
 *
 * @brief TopoDroid GPS-location for fixed stations
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120517 comments
 * 20120521 parented with SurveyActivity
 */
package com.android.DistoX;

// import java.Thread;
import java.util.Iterator;

import java.io.StringWriter;
import java.io.PrintWriter;

import android.app.Dialog;
import android.os.Bundle;

import android.util.Log;

import android.content.Context;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.GpsStatus;
import android.location.GpsSatellite;
// import android.location.GpsStatus.Listener;


public class DistoXLocation extends Dialog
                            implements View.OnClickListener
                                     , LocationListener
                                     , GpsStatus.Listener
{
  private static final String TAG = "DistoX Location";

  private boolean  mLocated;
  private LocationManager locManager;

  private TextView mTVlat;
  private TextView mTVlong;
  private TextView mTValt;
  private EditText mETstation;
  private Button   mBtnOK;
  private Button   mBtnCancel;
  private Button   mBtnStatus;

  private double latitude;  // decimal degrees
  private double longitude; // decimal degrees
  private double altitude;  // meters
  private SurveyActivity mParent;
  private GpsStatus mStatus;

  public DistoXLocation( Context context, SurveyActivity parent, LocationManager lm )
  {
    super(context);
    mParent = parent;
    locManager = lm;
    mStatus = locManager.getGpsStatus( null );
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // Log.v( TAG, "onCreate" );
    setContentView(R.layout.distox_location);
    mTVlat  = (TextView) findViewById(R.id.latitude  );
    mTVlong = (TextView) findViewById(R.id.longitude );
    mTValt  = (TextView) findViewById(R.id.altitude  );
    mETstation = (EditText) findViewById( R.id.station );
    mBtnStatus = (Button) findViewById( R.id.status );

    mBtnOK     = (Button) findViewById(R.id.button_ok );
    mBtnCancel = (Button) findViewById(R.id.button_cancel );

    mBtnOK.setOnClickListener( this );
    mBtnCancel.setOnClickListener( this );

    mBtnStatus.setBackgroundColor( 0x80ff0000 );
    
    mLocated = false;
    // locManager = (LocationManager) getSystemService( LOCATION_SERVICE );

    setTitle( R.string.title_location );
    locManager.addGpsStatusListener( this );
    locManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 1000, 0, this );
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    if ( b == mBtnOK && mLocated ) {
      // TODO use mETstation.getText()
      // TODO call back DistoX 
      mParent.addLocation( mETstation.getText().toString(), latitude, longitude, altitude);
      // Intent intent = new Intent();
      // intent.putExtra( TopoDroidApp.TOPODROID_LAT,  latitude );
      // intent.putExtra( TopoDroidApp.TOPODROID_LONG, longitude );
      // intent.putExtra( TopoDroidApp.TOPODROID_ALT,  altitude );
      // setResult( Activity.RESULT_OK, intent );
    } else {
      // setResult( RESULT_CANCELED );
    }
    // finish();
    locManager.removeUpdates( this );
    locManager.removeGpsStatusListener( this );
    dismiss();
  }

  public void onLocationChanged( Location loc )
  {
    displayLocation( loc );
    mLocated = true;
  }

  // location is stored in decimal degrees but displayed as deg:min:sec
  private void displayLocation( Location loc )
  {
    double sp  = loc.getLatitude();
    double sl = loc.getLongitude();
    latitude  = sp;
    longitude = sl;
    altitude  = loc.getAltitude();
    int dp = (int)sp;
    sp = 60*(sp - dp);
    int mp = (int)sp;
    sp = 60*(sp - mp);
    int dl = (int)sl;
    sl = 60*(sl - dp);
    int ml = (int)sl;
    sl = 60*(sl - ml);
    StringWriter swp = new StringWriter();
    PrintWriter pwp = new PrintWriter( swp );
    pwp.format( "%dd %02d\' %.2f\"", dp, mp, sp );
    StringWriter swl = new StringWriter();
    PrintWriter pwl = new PrintWriter( swl );
    pwl.format( "%dd %02d\' %.2f\"", dl, ml, sl );
    mTVlat.setText( swp.getBuffer().toString() );
    mTVlong.setText( swl.getBuffer().toString() );
    mTValt.setText( Double.toString( altitude ) );
  }

  public void onProviderDisabled( String provider )
  {
  }

  public void onProviderEnabled( String provider )
  {
  }

  public void onStatusChanged( String provider, int status, Bundle extras )
  {
    Log.v(TAG, "onStatusChanged status " + status );
  }

  public void onGpsStatusChanged( int event ) 
  {
    if ( event == GpsStatus.GPS_EVENT_SATELLITE_STATUS ) {
      locManager.getGpsStatus( mStatus );
      Iterator< GpsSatellite > sats = mStatus.getSatellites().iterator();
      int  nr = 0;
      while( sats.hasNext() ) {
        GpsSatellite sat = sats.next();
        if ( sat.usedInFix() ) ++nr;
      }
      // Log.v(TAG, "nr satellites used in fix " + nr );
      mBtnStatus.setText( Integer.toString( nr ) );
      switch ( nr ) {
        case 0: mBtnStatus.setBackgroundColor( 0x80ff0000 );
                break;
        case 1: mBtnStatus.setBackgroundColor( 0x80993333 );
                break;
        case 2: mBtnStatus.setBackgroundColor( 0x80663333 );
                break;
        case 3: mBtnStatus.setBackgroundColor( 0x80339933 );
                break;
        default: mBtnStatus.setBackgroundColor( 0x8000ff00 );
                break;
      }
      Location loc = locManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
      displayLocation( loc );
    }
  }
}

