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
 * 20120603 fixed-info list
 * 20120726 TopoDroid log
 */
package com.android.DistoX;

// import java.Thread;
import java.util.Iterator;

import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.List;

import android.app.Dialog;
import android.os.Bundle;

// import android.util.Log;

import android.content.Context;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
// import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.view.View;
import android.widget.ListView;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.GpsStatus;
import android.location.GpsSatellite;
// import android.location.GpsStatus.Listener;


public class DistoXLocation extends Dialog
                            implements View.OnClickListener
                                     , AdapterView.OnItemClickListener
                                     , LocationListener
                                     , GpsStatus.Listener
{
  // private boolean  mLocated;
  private LocationManager locManager;
  private Context mContext;
  private TopoDroidApp app;

  private TextView mTVlat;
  private TextView mTVlong;
  private TextView mTValt;
  private EditText mETstation;
  private Button   mBtnLoc;
  private Button   mBtnAdd;
  private Button   mBtnStatus;
  private ListView mList;
  private DistoXFixedAdapter mFixedAdapter;
  // private TextView  mSaveTextView;
  private int mSavePos;
  private FixedInfo mSaveFixed;

  private double latitude;  // decimal degrees
  private double longitude; // decimal degrees
  private double altitude;  // meters
  private SurveyActivity mParent;
  private GpsStatus mStatus;
  private boolean mLocating; // whether is locating

  public DistoXLocation( Context context, SurveyActivity parent, TopoDroidApp _app, LocationManager lm )
  {
    super(context);
    mContext = context;
    mParent = parent;
    app = _app;
    locManager = lm;
    mStatus = locManager.getGpsStatus( null );
    mLocating = false;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TopoDroidApp.Log( TopoDroidApp.LOG_LOC, "Location onCreate" );
    setContentView(R.layout.distox_location);
    mTVlong = (TextView) findViewById(R.id.longitude );
    mTVlat  = (TextView) findViewById(R.id.latitude  );
    mTValt  = (TextView) findViewById(R.id.altitude  );
    mETstation = (EditText) findViewById( R.id.station );
    mBtnStatus = (Button) findViewById( R.id.status );

    mList = (ListView) findViewById(R.id.list);
    mList.setOnItemClickListener( this );
    refreshList();

    mBtnLoc = (Button) findViewById( R.id.button_loc );
    mBtnAdd = (Button) findViewById(R.id.button_add );
    mBtnLoc.setOnClickListener( this );
    mBtnAdd.setOnClickListener( this );

    mBtnAdd.setEnabled( false );
    mBtnStatus.setBackgroundColor( 0x80ff0000 );
    // mBtnLoc.setText( getResources.getString( R.string.button_start ) );
    
    // mLocated = false;
    // locManager = (LocationManager) getSystemService( LOCATION_SERVICE );
    mLocating = false;
    setTitle( R.string.title_location );
  }

  public void refreshList()
  {
    List< FixedInfo > fxds = app.mData.selectAllFixed( app.mSID, TopoDroidApp.STATUS_NORMAL );
    mFixedAdapter = new DistoXFixedAdapter( mContext, R.layout.message, fxds );
    mList.setAdapter( mFixedAdapter );
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    // TopoDroidApp.Log.v( TopoDroidApp.LOG_LOC, "Location::onItemClick pos " + pos );
    // CharSequence item = ((TextView) view).getText();
    // String value = item.toString();
    // // setListPos( position  );
    // mSaveTextView = (TextView) view;
    mSaveFixed = mFixedAdapter.get(pos);
    mSavePos   = pos;
    (new FixedDialog( mContext, mParent, this, mSaveFixed )).show();
  }

  public void updatePos( )
  {
    mFixedAdapter.insert( mSaveFixed, mSavePos );
    mList.invalidate();
  }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;
    if ( b == mBtnAdd ) {
      if ( mETstation.getText() != null ) {
        String name = mETstation.getText().toString();
        if ( name.length() > 0 ) {
          FixedInfo f = mParent.addLocation( name, longitude, latitude, altitude);
          // mFixedAdapter.add( f.toString() );
          mFixedAdapter.add( f );
        }
      }
    } else if ( b == mBtnLoc ) {
      if ( mLocating ) {
        mBtnLoc.setText( mContext.getResources().getString( R.string.button_start ) );
        locManager.removeUpdates( this );
        locManager.removeGpsStatusListener( this );
        mLocating = false;
        setTitle( R.string.title_location );
      } else {
        mBtnLoc.setText( mContext.getResources().getString( R.string.button_stop ) );
        mBtnAdd.setEnabled( false );
        mBtnStatus.setBackgroundColor( 0x80ff0000 );
        locManager.addGpsStatusListener( this );
        locManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 1000, 0, this );
        mLocating = true;
        setTitle( R.string.title_location_gps );
      }
    }
  }

  @Override
  public void onBackPressed()
  {
    if ( mLocating ) {
      locManager.removeUpdates( this );
      locManager.removeGpsStatusListener( this );
      mLocating = false;
    }
    dismiss();
  }

  @Override
  public void onLocationChanged( Location loc )
  {
    displayLocation( loc );
    // mLocated = true;
  }

  // location is stored in decimal degrees but displayed as deg:min:sec
  private void displayLocation( Location loc )
  {
    if ( loc != null ) {
      double sp = loc.getLatitude();
      double sl = loc.getLongitude();
      latitude  = sp;
      longitude = sl;
      altitude  = loc.getAltitude();

      mTVlat.setText( mContext.getResources().getString( R.string.latitude ) + " " + FixedInfo.double2ddmmss( sp ) );
      mTVlong.setText( mContext.getResources().getString( R.string.longitude ) + " " + FixedInfo.double2ddmmss( sl ) );
      mTValt.setText( mContext.getResources().getString( R.string.altitude ) + " " + Integer.toString( (int)(altitude) ) );
    } else {
      TopoDroidApp.Log(TopoDroidApp.LOG_ERR, "displayLocation null");
    }
  }

  public void onProviderDisabled( String provider )
  {
  }

  public void onProviderEnabled( String provider )
  {
  }

  public void onStatusChanged( String provider, int status, Bundle extras )
  {
    // TopoDroidApp.Log(TopoDroidApp.LOG_LOC, "onStatusChanged status " + status );
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
      // TopoDroidApp.Log(TopoDroidApp.LOG_LOC, "onGpsStatusChanged nr satellites used in fix " + nr );
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
      mBtnAdd.setEnabled( nr >= 4 );

      try {
        Location loc = locManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
        displayLocation( loc );
      } catch ( IllegalArgumentException e ) {
        TopoDroidApp.Log(TopoDroidApp.LOG_ERR, "onGpsStatusChanged IllegalArgumentException " );
      } catch ( SecurityException e ) {
        TopoDroidApp.Log(TopoDroidApp.LOG_ERR, "onGpsStatusChanged SecurityException " );
      }
    }
  }
}

